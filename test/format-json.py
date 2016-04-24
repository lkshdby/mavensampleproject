#!/usr/bin/python

import sys,json

if __name__ == "__main__":
    input = sys.stdin.read()
    try:
        data=json.loads(input)
        print json.dumps(data, indent=2)
    except:
        print input
    sys.exit(0)
