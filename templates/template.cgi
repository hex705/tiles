#!/usr/bin/python

import xml.etree.ElementTree as ET
import re
import os.path
import glob
from string import Template	
import cgi
import os
import zipfile
import sys
import uuid
import shutil
from time import gmtime, strftime

import emailer

# external dependency
# pip install jsbeautifier 
import jsbeautifier

debugMode = False


#=========================================================================================
def zipdir(path, zip):
    for root, dirs, files in os.walk(path):
        for file in files:
			fileToZip = os.path.join(root, file)
			fileName = os.path.join(root.replace(path,"")[1:], file)
			zip.write(fileToZip, fileName)

#=========================================================================================
hubs = {} # contains tile names --> string templates
templates = {} # contains tile names --> xml templates
codes = {} # contains tile id --> code templates 

#=========================================================================================
def htmlHeaders():
	print 'Content-type: text/html\n\n'	

#=========================================================================================
def debug(str):
	if debugMode: print str

#=========================================================================================
def error(msg):
	if debugMode: print "ERROR:", msg

#=========================================================================================
def tileName(filename):
	base = os.path.basename(filename)
	return os.path.splitext(base)[0]

#=========================================================================================
def loadTemplates():
	# load all 'hub' templates 
	debug("="*80)
	for file in glob.glob("tiles/*.hub"):
		tile = tileName(file)
		debug("Loaded %s template" % tile)	
		hubs[tile] = open(file).read()
	# load all other tile templates
	for file in glob.glob("tiles/*.xml"):
		tile = tileName(file)
		debug("Loaded %s template" % tile)
		templates[tile] = ET.parse(file)

#=========================================================================================	
def parseGraph(xml):
	for path in xml.findall('./paths/path'):
		parsePath(path)

#=========================================================================================					
def parsePath(xml):
	debug("="*80)
	nodes = getNodeListFromPath(xml)
	
	# Rule 1: the first node should be a hub
	hub = nodes[0]
	if hub.type != 'hub': 
		error("First node is not a Hub")
		return 
	
	# Rule 2: the last node should be a hub also
	if nodes[-1].type != 'hub':
		error("Last node is not a Hub")
		return

	# Create a new code template if we don't have this tile already
	if hub.id not in codes:
		debug("Created new code template for %s, id %s" % (hub.name, hub.id))
		codes[hub.id] = Code(hub.name)  		

	# Get the template for this hub 
	code = codes[hub.id]
	
	# feed it all the other nodes except the first one
	code.processPath(nodes[1:])
		
	
#=========================================================================================					
def getNodeListFromPath(xml):
	nodesXML = xml.findall('./node')
	nodes = []
	for node in nodesXML:
		id = node.find('id').text
		name = node.find('tileName').text
		topcode = node.find('topcodeID').text
		type = node.find('type').text			
		nodes.append( Node(id, name, topcode, type) )
	return nodes
		
#=========================================================================================
class Node:
	id = 0
	topcode = 0
	tile = ""
	type = ""
	
	def __init__(self, id, tile, topcode, type):
		self.id = id
		self.name = tile
		self.topcode = topcode
		self.type = type
		
	def __str__(self):
		return "%s, %s, %s, %s" % (self.id, self.name, self.topcode, self.type)

#=========================================================================================
class Code:
 
	#-------------------------------------------------------------------------------------
	def generate(self):
		for section, code in self.unique.iteritems():
			self.everything[section] += code
		for section, code in self.always.iteritems():
			self.everything[section] += code
		for section, code in self.default.iteritems():
			self.everything[section] += code	
		
		# plug each code section into the base template
		for section, code in self.everything.iteritems():
			self.base = self.base.replace(section, code) 
	
		# format // prettify 
		result = jsbeautifier.beautify(self.base)		
		return result
	
	#-------------------------------------------------------------------------------------
	def __init__(self, hub):
		self.hub = hub
		self.hubPrefix = hub[0:3].upper()
		self.base = hubs[hub]
		# find all the $sections in the base template 
		sections = re.findall('\$\w+', self.base)		
		# create dictionaries for all the sections
		self.unique     = dict(zip(sections, [""]*len(sections)))
		self.always     = dict(zip(sections, [""]*len(sections)))
		self.default    = dict(zip(sections, [""]*len(sections)))
		self.everything = dict(zip(sections, [""]*len(sections)))
		
		self.counters = {}
		self.nodesAdded = []
		
		self.prevNode = None

	#-------------------------------------------------------------------------------------
	def processPath(self, nodes):
		
		if self.hub == "Arduino":
			# create a fake node for ARD-Serial at the beginning 
			# this will cause "ARD-Serial" to get include 
			nodes.insert(0, Node(-1, "ARD-Serial", -1, -1)) 
			
		for node in nodes:
			if node.type == "hub":
				debug("Found a hub, we're done")
				break
			else:
				# SPECIAL CASE: XBee nodes -- they don't add any code 
				if node.name.find("xBee") != -1: 
					debug("--> Found an XBee node. Need to deal with this.")
					continue 

				# SPECIAL CASE:
				# Arduino -> Serial -> Processing 
				if node.name != "OSC" and node.name.find(self.hubPrefix) == -1:
					debug("Our hub is %s but we're now into tile %s, so we're done" % (self.hubPrefix, node.name))
					break
				
				
				self.processNode(node)
				self.prevNode = node
				if node.type == "data_protocol":
					debug("Found a protocol, we're done")
					break
			
		# take code from the default section, and move it to always
		for section in self.default:
			self.always[section] += self.default[section]

		# clear out the default section
		for section in self.default: self.default[section] = ""
				
	#-------------------------------------------------------------------------------------	
	def processNode(self, node):
		debug("Adding node %s" % node)
		
		# get the XML document associated with this node
		xml = self.getXML(node.name)
			
		# register/increment counters
		cxml = xml.findall('counter')	
		for c in cxml:
			try:
				self.counters[c.text] += 1
			except KeyError:
				self.counters[c.text] = 1 
				debug("Registered counter " + c.text)	
		
		# process each section
		if node not in self.nodesAdded:
			self.processSection(xml, 'unique')
			self.nodesAdded.append(node)
		self.processSection(xml, 'always')
		self.processSection(xml, 'default')
	
					
	#-------------------------------------------------------------------------------------			
	def getXML(self, nodeName):		
		
		# First attempt: do we have an XML file for this node name 
		if nodeName in templates.keys():
			return templates[nodeName] 
		
		# If not, then we must handle the special case
		if nodeName == "OSC": 
			# the OSC node is odd, because it depends what came before it 
			prefix = self.hub[0:3].upper()
			
			# if there is a node before 
			if self.prevNode != None: 
				if self.prevNode.name == "PRO-Serial": suffix = "Serial"
				if self.prevNode.name == "PRO-Internet": suffix = "Internet"
				if self.prevNode.name == "ARD-Wifi": suffix = "Internet"
				if self.prevNode.name == "ARD-Ethernet": suffix = "Internet"	
				if self.prevNode.name == "ARD-Serial": suffix = "Serial"			
			else:
				error("Cannot find template for tile %s" % nodeName)
			
		 	t = templates["%s-%s-%s" % (prefix, nodeName, suffix)]
			return t
					
	#-------------------------------------------------------------------------------------			
	def processSection(self, xml, whichOne):		
		section = xml.find(whichOne)	
		if whichOne == 'default' and section != None:
			# reset the default section 
			for s in self.default: self.default[s] = ""
		
		for sub in section:
			key = "$" + sub.tag

			content = sub.text
			if content == None: continue
			
			# remove extra newlines and whitespace
			content = content.strip()
			
			# replace counters, if needed
			for counter, value in self.counters.iteritems():
				if value > 1: content = content.replace(counter, str(value))
				# if the value is 1, just put nothing
				# eg:
				# serial and serial2, instead of serial1 and serial2
				else: content = content.replace(counter, "")

			# add this code to the right section 
			if whichOne == 'unique':
				self.unique[key] += content
			if whichOne == 'always':
				self.always[key] += content
		 	if whichOne == 'default':
				self.default[key] = content  

		
#=========================================================================================
if __name__ == "__main__":
	
	if len(sys.argv) >= 2:
		testFile = sys.argv[1]
		xml = ET.parse(testFile)	
		debugMode = True
	else:
		# assume we're running as CGI
		htmlHeaders()
		debugMode = False
		form = cgi.FieldStorage()
		if 'message' in form:
			# use CGI mode
			xml = ET.fromstring(form['message'].value)
		else:
			print "DOH"
			sys.exit()		
	
	loadTemplates()
	parseGraph(xml)	
	
	# TODO: generate source files and folders 
	# from this 
	
	baseDir = str(uuid.uuid4())
	print "Creating temporary folder: ", baseDir
	os.makedirs(baseDir)
	
	timestamp = strftime("%Y-%m-%d_%Hh%M", gmtime())	
	projectDir = os.path.join(baseDir, "%s_templates" % timestamp)
	os.makedirs(projectDir)
	
	counts = {}
	for code in codes.itervalues():
	
		# this will keep track of similar tiles (eg: 2x Processing) and count them 
		# to create different folders
		num = 1
		if code.hub in counts:
			counts[code.hub] += 1
			num = counts[code.hub]
		else:
			counts[code.hub] = 1
		
	
		print "Generating code for", code.hub, num		
		
		templateDir = os.path.join(projectDir, "%s%s" % (code.hub, num))
		os.makedirs(templateDir)
		
		# Create the source file
		if code.hub == "Processing": sourceFile = "%s%s.pde" % (code.hub, num)
		if code.hub == "Arduino": sourceFile = "%s%s.ino" % (code.hub, num)
		if code.hub == "Android": sourceFile = "%s%s.pde" % (code.hub, num)

		sourcePath = os.path.join(templateDir, sourceFile) 		
		
		file = open(sourcePath, 'w')		
		source = code.generate()		
		file.write(source)		
		file.close()
	
	# zip it all
	zipFileName = '%s.zip' % baseDir
	zip = zipfile.ZipFile(zipFileName, 'w')
	zipdir(baseDir, zip) 
	zip.close()
		
	# clean up
	#shutil.rmtree(baseDir)
	
	# send the file via email
	emailAddr = xml.find('email').text
	print emailAddr
	#  send_mail("dbouchard@gmail.com", "testing", "hello", "test.zip") 		
	#emailer.send_mail(emailAddr, "Your code template", "Testing", zipFileName, "template.zip")
	
	os.remove(zipFileName)
	