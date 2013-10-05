import smtplib

from email import encoders
from email.message import Message
from email.mime.audio import MIMEAudio
from email.mime.base import MIMEBase
from email.mime.image import MIMEImage
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText

import mimetypes

import csv
import sys

# USAGE: 
# run using "python emailer.py" to test the script 
# run using "python emailer.py go" to actually send the emails. it will ask
# for a confirmation before sending each email 
 
server = "smtp.gmail.com"
login  = "tangibletiles@gmail.com"

pw     = "tilestangible"  # <--- use a Google Token here 
# http://www.ryerson.ca/google/usingapps/googletoken.html
 

#==============================================================================
def send_mail(to, subject, text, zipfile, zipname="testing.zip"):

  message = MIMEMultipart()
 
  ctype = 'application/zip'
  maintype, subtype = ctype.split('/', 1)
  
  fp = open(zipfile,'rb')
  attachment = MIMEBase(maintype, subtype)
  attachment.set_payload(fp.read())
  encoders.encode_base64(attachment)
  fp.close()
  
  attachment.add_header('Content-Disposition', 'attachment', filename=zipname)
  message.attach(attachment)
  
  
  message["To"] = to
  message["From"] = login
  message["Subject"] = subject
  message.preamble = text
  #message.set_payload(text)
  
  mailServer = smtplib.SMTP(server, 587) 
  mailServer.ehlo()
  mailServer.starttls()
  mailServer.ehlo()
  mailServer.login(login, pw)
  print "Result:", mailServer.sendmail(login, to, message.as_string())
  mailServer.quit()
 
#==============================================================================
if __name__ == "__main__":
  send_mail("dbouchard@gmail.com", "testing", "hello", "test.zip") 		