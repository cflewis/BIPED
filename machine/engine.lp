% Core structural rules for any answer set programming based game modeling.
% The "game engine" that lifts lparse to a game-domain specific language.
%
% Adam Smith (amsmith@cs.ucsc.edu)
% November 2, 2008

% Implementation of all %!!!-noted items is left to the game designer.

%%%%
%%%% TIMELESS STATE
%%%%

%!!! Game SHOULD describe (in comments) a lanugage for the design of a
%!!! particular game world (without reference to T).

%%%%
%%%% GAME STATE
%%%%

%% LANGUAGE

#domain game_state(GS).
#domain game_state(GS1).
#domain game_state(GS2).
fluent(GS).

%!!! Game MUST specify game_state(GS) describing a language of game state fluents.

%~ % Circumscription of initial conditions
%~ -holds_at(GS,0) :- game_state(GS), not holds_at(GS,0).

%% INITIAL CONDITIONS

%!!! Game SHOULD specifiy holds_at(GS,0) describing initial conditions.

%% DERIVED STATE

%!!! Game MAY specify additional predicates that describe derived game state.


%%%%
%%%% GAME EVENTS
%%%%

%% LANGUAGE


#domain game_event(GE).
#domain game_event(GE2).
#domain game_event(GE2).
event(GE).

%!!! Game MUST specify game_event(GE) describing a language of world-level events.

%% NECESSARY CONDITIONS

%!!! Game SHOULD specify possible(GE,T) describing necessary conditions.
%!!! Game MAY specify conflicts(GE1,GE1,T) describing impossible interactions.

%% SUFFICIENT CONDITIONS

% This rule delegates the selection (via abduction or direct assumption) of game
% events to the selected(GE,T) predicate which is later described in terms of
% natural and player events.  

happens(GE,T) :-
	game_event(GE),
	possible(GE,T),
	selected(GE,T),
	not conflicted(GE,T),
	not forbidden(GE,T).

{ happens(GE,T) } :-
	game_event(GE),
	player_or_natural_event(GE),
	possible(GE,T),
	not conflicted(GE,T),
	not forbidden(GE,T).

player_or_natural_event(GE) :- player_event(GE).
player_or_natural_event(GE) :- natural_event(GE).

conflicted(GE1,T) :-
	possible(GE2,T),
	happens(GE2,T),
	conflicts(GE1,GE2,T).

%% DIRECT CONSEQUENCES

%!!! Game SHOULD specify initiates(GE,GS,T) describing direct effects of game
%!!! events.

%%%%
%%%% NATURAL EVENTS
%%%%

% Natural events are those that conditionally occur outside of the direct
% control of the player.  Natural events may describe secondary events spawned
% by player events or other natural events, or they may encode nondeterministic
% events the may occur spontaneously.

%% LANGUAGE

%domain natural_event(NE).

%!!! Game MUST specify natural_event(NE) describing a language of
%!!! nature-selected events.

%% DEDUCTIVE SELECTION

selected(NE,T) :-
	natural_event(NE),
	possible(NE,T),
	nature_asserts(NE,T).

%!!! Game MAY specify nature_asserts(NE,T) describing sufficient conditions for
%!!! _deriving_ the selection of a natural event.

%% NEGATIVE SELECTION

forbidden(NE,T) :-
	natural_event(NE),
	nature_forbids(NE,T).

%!!! Game MAY specify nature_forbids(NE,T) describing conditions for blocking
%!!! selection of a natural event.

%%%%
%%%% PLAYER EVENTS
%%%%

% Player events are those that conditionally occur within of the direct control
% of the player.

%% LANGUAGE

#domain player_event(PE).

%!!! Game MUST specify player_event(PE) describing a language of
%!!! player-selected events.

%% DEDUCTIVE SELECTION

selected(PE,T) :-
	player_event(PE),
	possible(PE,T),
	player_asserts(PE,T).

%!!! Game MAY specify player_asserts(PE,T) describing sufficient conditions for
%!!! _deriving_ the selection of a player event.

%% NEGATIVE SELECTION

forbidden(PE,T) :-
	player_event(PE),
	player_forbids(PE,T).

%!!! Game MAY specify player_forbids(PE,T) describing conditions for blocking
%!!! selection of a player event.

