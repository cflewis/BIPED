% A simple discrete event calculus.
%
% Adam Smith (amsmith@cs.ucsc.edu)
% November 2, 2008

#domain fluent(F).
#domain fluent(F1).
#domain fluent(F2).

#domain event(E).
#domain event(E1).
#domain event(E2).

#domain timepoint(T).
#domain timepoint(T1).
#domain timepoint(T2).

initiated(F,T) :-
	happens(E,T),
	initiates(E,F,T).

terminated(F,T) :-
	happens(E,T),
	terminates(E,F,T).

holds_at(F,T+1) :-
	holds_at(F,T),
	not terminated(F,T).

-holds_at(F,T+1) :-
	-holds_at(F,T),
	not initiated(F,T).

holds_at(F,T+1) :-
	happens(E,T),
	initiates(E,F,T).

-holds_at(F,T+1) :-
	happens(E,T),
	terminates(E,F,T).

