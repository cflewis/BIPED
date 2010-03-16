pos(go).
pos(jail).
pos(park_place).
pos(boardwalk(santa_cruz)).

ui_space(P) :- pos(P).

ui_title('Monopoly!!!!!').

piece(dog).
piece(top_hat).
piece(earl(sandwich)).

ui_token(P) :- piece(P).

ui_autolayout(circular).

next(go,jail).
next(jail,park_place).
next(park_place,boardwalk(santa_cruz)).
next(boardwalk(santa_cruz),go).

ui_path(A,B) :- next(A,B).

game_state(is_at(Piece,Pos)) :- piece(Piece),pos(Pos).

initially(is_at(P,go)) :- piece(P).

ui_location(P,S) :- holds(is_at(P,S)).

game_event(push(P)) :- piece(P).

ui_triggers(ui_click_token(P), push(P)) :- piece(P).
ui_triggers(ui_click_space(Pos), push(Piece)) :- holds(is_at(Piece,Pos)).

possible(push(P)) :- piece(P).

ui_details(t=T) :- timepoint(T).

initiates(push(P), is_at(P,New)) :- holds(is_at(P,Old)),next(Old,New).
terminates(push(P),is_at(P,Old)) :- holds(is_at(P,Old)).

ui_instruction('I fail to click on compound terms, what gives?').
