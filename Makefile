#
# Makefile responsible for building the EC-CloudManager plugin
#
# Copyright (c) 2005-2012 Electric Cloud, Inc.
# All rights reserved

SRCTOP=..
include $(SRCTOP)/build/vars.mak

build: buildJavaPlugin
unittest: junit gwttest
systemtest:

include $(SRCTOP)/build/rules.mak

generateSrc:
	$(ANT) $(ANTFLAGS) generate.src
