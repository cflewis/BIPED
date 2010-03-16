% this code implements the runtime glue to replace the lparse engine

%%%%
%%%% default cases (logically fail instead of causing interpreter barf)
%%%%

%% mechanics

game_state(Fluent) :- fail.
game_event(Event) :- fail.

initially(Fluent) :- fail.
possible(Event) :- fail.
conflicts(Event1,Event2) :- fail.

initiates(Event,Fluent) :- fail.
terminates(Event,Fluent) :- fail.

%% repr binding

% timeless
ui_triggers(UiEvent,GameEvent) :- fail.
ui_title(Title) :- fail.
ui_instruction(Title) :- fail.
ui_token(Token) :- fail.
ui_space(Space) :- fail.
ui_path(Space1,Space2) :- fail.
ui_ticker(Interval) :- fail.
ui_timer(Delay) :- fail.
ui_soundtrack(Resource) :- fail.

ui_autolayout(_) :- fail.
ui_layout('__outOfPlay',-0.1,1.1).

% transient
ui_location(Token,Space) :- fail.
ui_location_total(Token,Space) :- ui_location(Token,Space).
ui_location_total(Token,'__outOfPlay') :- ui_token(Token), \+ ui_location(Token,_).
ui_details(Entry) :- fail.

% events:
%   ui_click_space(Space)
%   ui_click_token(Token)
%   ui_tick(TickNumber)
%   ui_timeout

%%%%
%%%% runtime logic
%%%%

%% event calculus

initiated(Fluent) :- happens(Event), initiates(Event,Fluent).
terminated(Fluent) :- happens(Event), terminates(Event,Fluent).

holds(Fluent) :- holds_transiently(Fluent).

holds_next(Fluent) :- initiated(Fluent).
holds_next(Fluent) :- holds(Fluent), \+ terminated(Fluent).

%% event triggering

happens(Event) :- happens_spontaneously(Event).
happens(GameEvent) :-
    happens_spontaneously(UserEvent),
    ui_triggers(UserEvent,GameEvent),
    possible(GameEvent).


ui_interesting(UserEvent) :-
    ui_triggers(UserEvent,GameEvent),
    possible(GameEvent).

%% initial conditions
timepoint(0).


%%%%
%%%% simple validation
%%%%

design_issue(no_game_state) :- \+ game_state(_).
design_issue(no_game_event) :- \+ game_event(_).

design_issue(unused_game_state(Fh/A)) :- game_state(F), functor(F,Fh,A), \+ (initiates(_,F); terminates(_,F)).
design_issue(unused_game_event(Eh/A)) :- game_event(E), functor(E,Eh,A), \+ (initiates(E,_); terminates(E,_)).

design_issue(undeclared_state(Fh)) :- (initiates(_,F); terminates(_,F)), functor(F,Fh,A), \+ game_state(F).
design_issue(undeclared_event(Eh)) :- (initiates(E,_); terminates(E,_)), functor(E,Eh,A), \+ game_event(E).

design_issue(no_title) :- \+ ui_title(_).
design_issue(no_instruction) :- \+ ui_instruction(_).

design_issue(no_tokens) :- \+ ui_token(_).
design_issue(no_spaces) :- \+ ui_space(_).

design_issue(ignores_ui)  :- \+ ui_triggers(_,_).

initates(_,_) :- fail.
teriminates(_,_) :- fail.
ui_trigger(_,_) :- fail.
design_issue(defines_initates) :- initates(_,_).
design_issue(defines_teriminates) :- teriminates(_,_).
design_issue(defines_ui_trigger) :- ui_trigger(_,_).

%%%%
%%%% prolog library classics
%%%%

forall(Condition,Action) :- Condition, Action, fail.
forall(_,_).

% eof