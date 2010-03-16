#!/usr/bin/perl
while(<>) {
	if (/^Stable/) {
		s/.*?: //;
		s/ /.\n/g;
		print;
	} else {
		print "%-- $_";
	}
}
