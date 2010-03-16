LPS = $(shell cat lps.list)
LPARSE_FLAGS = $(shell cat lparse.flags)
SMODELS_FLAGS = $(shell cat smodels.flags)
PROLOG = swipl

all: rules.lp problem solution

rules.lp: script.pl
	$(PROLOG) -g '[compiler,script], main.' > rules.lp

problem: $(LPS) lparse.flags lps.list
	lparse $(LPARSE_FLAGS) $(LPS) | pv -N grounding > problem

solution: problem smodels.flags
	pv -N solving problem | smodels $(SMODELS_FLAGS) > solution


.PHONY: clean 
clean:
	-rm problem
	-rm solution
	-rm rules.lp
