#!/usr/bin/env python

# --------------------------------------------------------------------------
# File: setup.py
# ---------------------------------------------------------------------------
# Licensed Materials - Property of IBM
# 5725-A06 5725-A29 5724-Y48 5724-Y49 5724-Y54 5724-Y55
# Copyright IBM Corporation 2008, 2011. All Rights Reserved.
#
# US Government Users Restricted Rights - Use, duplication or
# disclosure restricted by GSA ADP Schedule Contract with
# IBM Corp.
# ------------------------------------------------------------------------

"""
setup.py file for the CPLEX Python API 
"""

import platform

from distutils.core import setup, Extension
from sys import api_version

so_ext='so'

if api_version == 1012:
    data = ['py1012_cplex123.' + so_ext]
elif api_version == 1013:
    data = ['py1013_cplex123.' + so_ext]    
        
setup(name = 'cplex',
      version = '12.3.0b0',
      author = "IBM",
      description = """A Python interface to the CPLEX Callable Library.""",
      packages = ['cplex',
                  'cplex._internal',
                  'cplex.exceptions'],
      package_dir = {'cplex': 'cplex',
                     'cplex._internal': 'cplex/_internal',
                     'cplex.exceptions': 'cplex/exceptions'},
      package_data = {'cplex._internal': data},
      url = 'http://www-01.ibm.com/software/websphere/products/optimization/',
      )
