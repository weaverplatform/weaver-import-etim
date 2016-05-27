import xml.etree.ElementTree as ET
ET.register_namespace('', "http://www.etim.nl/etimixf-rt/1")
tree = ET.parse('ETIMIXFRT1_1_20160216.xml')
root = tree.getroot()
for child in root:
	tag = child.tag.split('}', 1)[1]
	filename = format(tag + "Unite.xml")
	with open(filename, 'wb') as f:
		f.write(ET.tostring(child).replace(' xmlns="http://www.etim.nl/etimixf-rt/1"',''))
