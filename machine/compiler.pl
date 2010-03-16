engine_pred(game_event/1).
engine_pred(game_state/1).
engine_pred(player_event/1).
engine_pred(natural_event/1).
engine_pred(possible/1).
engine_pred(conflicts/2).
engine_pred(initiates/2).
engine_pred(terminates/2).
engine_pred(initially/1).
engine_pred(player_asserts/1).
engine_pred(player_forbids/1).
engine_pred(nature_asserts/1).
engine_pred(nature_forbids/1).

native_pred(true/0).
native_pred((\=)/2).
native_pred((\+)/1).
native_pred((is)/2).

expand(holds(F), holds_at(F,'T')).
expand(initiates(E,F), initiates(E,F,'T')).
expand(terminates(E,F), terminates(E,F,'T')).
expand(possible(E), possible(E,'T')).
expand(conflicts(E1,E2), conflicts(E1,E2,'T')).
expand(initially(F), holds_at(F, 0)).
expand(player_asserts(E), player_asserts(E,'T')).
expand(player_forbids(E), player_forbids(E,'T')).
expand(nature_asserts(E), nature_asserts(E,'T')).
expand(nature_forbids(E), nature_forbids(E,'T')).
expand(Term, NewTerm) :-
        Term =.. [Pred | Args],
        state_helper(Pred),
        append(Args, ['T'], NewArgs),
        NewTerm =.. [Pred | NewArgs].
expand(A is B, A == B).
expand(\+A, NewTerm) :- atom_concat('not ', A, NewTerm).

do_expand(Old, New) :- expand(Old, New) -> true; New=Old.


engine_preds(L) :- setof(T,engine_pred(T),L).

check(S, P) :- write(user_error, S), write(user_error, '  '), call(P) -> write(user_error, 'OK'), nl(user_error) ; write(user_error, 'NO').

engine_preds_ok :-
        engine_preds(Ps),
        forall(member(P,[design_ok/0|Ps]),(case(P,_);write(user_error, missing(P)),fail)).

design_ok :-
        case(design_error/1,_),  design_error(X), write(user_error, X), nl(user_error), !, fail.
design_ok :- true.

main :-
        check('engine predicates?', engine_preds_ok),
        check('design?', design_ok),
        all_preds(All),
        nl,
        write(user_error, 'predicates to be translated'), nl(user_error),
        writelist(All),
        forall(member(P,All),translate(P)),
        halt.

translate(P) :-
        forall(case(P,Term),
               (numbervars(Term, 0, _, [singletons(true)]),
                Term = (Head :- Body),
                write_lparse(Head),
                (Body == true ->
                   true ;
                   (write(' :- '),
                    comma_list(Body, [FirstTerm | RestTerms]),
                    write_lparse(FirstTerm),
                    forall(member(T, RestTerms),(write(', '),write_lparse(T))))),
                write('.'),
                nl)).

write_lparse(A \= B) :- write_lparse_parens(A), write(' != '), write_lparse_parens(B).
write_lparse(\+A) :- write('not '), write_lparse(A).
write_lparse(Term) :- do_expand(Term, NewTerm), write_term(NewTerm, [numbervars(true)]).

write_lparse_parens(Term) :- write('('), write_lparse(Term), write(')').


all_preds(All) :-
        engine_preds(Roots),
        all_preds(Roots,All).

all_preds(Roots,All) :-
        setof(Dep,R^(member(R,Roots),depends(R,Dep)),Deps),
        union(Roots, Deps, Union),
        (Roots = Union -> All = Union
                        ;  all_preds(Union,All)).

writelist([]).
writelist([H|T]) :- 
        write(user_error, '\t'), write(user_error, H), nl(user_error),
        writelist(T).


case(F/A, Head :- Body) :-
        functor(T,F,A), 
        nth_clause(T,_,R),clause(Head,Body,R).

comma_list((H,T),[H|L]) :- !, comma_list(T,L).
comma_list(H,[H]).

depends(P,Dep) :-
        case(P, _ :- Body),
        comma_list(Body,Terms),
        member(T,Terms),
        functor(T,DF,DA),
        Dep = DF/DA,
        \+ native_pred(Dep).
depends(P,Dep) :-
        case(P, _ :- Body),
        comma_list(Body,Terms),
        member(\+ T,Terms),
        functor(T,DF,DA),
        Dep = DF/DA,
        \+ native_pred(Dep).
