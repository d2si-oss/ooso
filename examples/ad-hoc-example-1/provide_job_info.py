#!/usr/bin/python3

import json
import os

json_opts = json.loads(input())
with(open(os.path.abspath(json_opts["path"]), "r")) as job_info_file:
    job_info_json = json.load(job_info_file)
    job_info_string = json.dumps(job_info_json)
    print(job_info_string)
