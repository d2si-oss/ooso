#!/usr/bin/python3


import uuid
import json

with(open("src/main/resources/jobInfo.json", "r")) as job_info_file_old:
    job_id = uuid.uuid1().urn[9:]
    job_info_json = json.load(job_info_file_old)
    job_info_json["jobId"] = job_id
    with(open("src/main/resources/jobInfo.json", "w")) as job_info_file_new:
        json.dump(job_info_json, job_info_file_new)
