import xml.etree.ElementTree as ET
import re
from string import Template	
import jsbeautifier

def run():
	base = open('Processing.hub').read()
	# find all the $sections
	sections = re.findall('\$\w+', base)
		
	subs = dict(zip(sections, [""]*len(sections)))
	print subs
	
	xml = ET.parse("Serial-Processing.xml")
	
	# apply the core elements to the template
	core = xml.find('core')
	for element in core:
		section = "$" + element.tag
		# remove extra newlines and whitespace
		content = element.text.strip()
		# set counters and addresses
		content = content.replace("$count", "")
		# add this code to the right section 
		if section in subs:
			subs[section] += content

	# substitute the section labels with the code 			
	for sub, code in subs.iteritems():
		base = base.replace(sub, code) 
	
	# format // prettify 
	result = jsbeautifier.beautify(base)
	print result
	

if __name__ == "__main__":	
	run()