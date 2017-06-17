import sys

def sxor(s1,s2):    
    # convert strings to a list of character pair tuples
    # go through each tuple, converting them to ASCII code (ord)
    # perform exclusive or on the ASCII code
    # then convert the result back to ASCII (chr)
    # merge the resulting array of characters as a string
    return ''.join(chr(ord(a) ^ ord(b)) for a,b in zip(s1,s2))

def main():
	s1 = sys.argv[1]
	s2 = sys.argv[2]
	filePath = sys.argv[3]
	product = sxor(s1,s2)	
	with open(filePath, "w") as f:
		f.write(product)

if __name__ == "__main__":
	main()

