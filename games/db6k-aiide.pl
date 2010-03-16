%%%%
%%%% GAME MECHANICS
%%%%

:- style_check(-discontiguous).
:- style_check(-atom).

%% decls

game_state(position(P)) :- pos(P).
game_state(energy(L)) :- level(L).

game_state(present(R)) :- rock(R).
game_state(bagged(R)) :- rock(R).
game_state(banked(R)) :- treasure(R).

game_event(mine(R)) :- rock(R).
game_event(up_to(P)) :- pos(P).
game_event(down_to(P)) :- pos(P).
game_event(refuel).
game_event(trade).
game_event(drain).

doesnt_conflict(trade, refuel).
doesnt_conflict(refuel, trade).
doesnt_conflict(E,E) :- game_event(E).
conflicts(E1,E2) :- game_event(E1), game_event(E2), \+ doesnt_conflict(E1,E2).

state_helper(occupied).
occupied(P) :- pos(P), rock(R), in(R,P), holds(present(R)).

state_helper(energized).
energized :- \+ holds(energy(0)).

%% nature

natural_event(drain).
%nature_asserts(drain) :- \+ (player_event(E), happens(E)).
nature_asserts(drain).

%% player

player_event(mine(R)) :- rock(R).
player_event(up_to(P)) :- pos(P).
player_event(down_to(P)) :- pos(P).
player_event(refuel).
player_event(trade).

%% consequences

initiates(mine(R),bagged(R)) :- rock(R).
initiates(mine(R),energy(L2)) :- rock(R), level(L1), level(L2),  
	holds(energy(L1)), L2 is L1-1.

terminates(mine(R),energy(L)) :- rock(R), level(L),
	holds(energy(L)).

terminates(mine(R),present(R)) :- rock(R).

initiates(up_to(P),energy(L2)) :- pos(P), level(L1), level(L2),
	holds(energy(L1)), L2 is L1-1.
initiates(up_to(P),position(P)) :- pos(P).

terminates(up_to(P),energy(L)) :- pos(P), level(L), holds(energy(L)).
terminates(up_to(P2),position(P1)) :- pos(P1), pos(P2), holds(position(P1)).

initiates(down_to(P),position(P)) :- pos(P).
terminates(down_to(P2),position(P1)) :- pos(P1), pos(P2), holds(position(P1)).

initiates(refuel,energy(L)) :- max_energy(L).
terminates(refuel,energy(L)) :- level(L), holds(energy(L)), \+ max_energy(L).

initiates(trade,banked(T)) :- holds(bagged(T)), treasure(T).
terminates(trade,bagged(R)) :- rock(R), holds(bagged(R)).

initiates(drain,energy(L2)) :- level(L1), level(L2),
	holds(energy(L1)), L2 is L1-1.
terminates(drain,energy(L)) :- level(L), holds(energy(L)).

%% conditions

possible(mine(R)) :- holds(present(R)), holds(position(P1)), link(P2,P1), in(R,P2), energized.
possible(mine(R)) :- holds(present(R)), holds(position(P1)), link(P1,P2), in(R,P2), energized.

possible(up_to(P2)) :- holds(position(P1)), link(P2,P1), energized, \+ occupied(P2).
possible(down_to(P2)) :- holds(position(P1)), link(P1,P2), \+ occupied(P2).
possible(refuel) :- holds(position(base)).
possible(trade) :- holds(position(base)).

possible(drain) :- energized.

%% initials

initially(position(base)).
initially(energy(L)) :- max_energy(L).

initially(present(R)) :- rock(R).
% no bag
% no bank

%%%%
%%%% LEVEL DESIGN
%%%%

pos(base).

pos(a).
pos(b).
pos(c).
pos(d).
pos(e).
pos(f).
pos(g).
pos(h).
pos(i).

% downward links
link(base,a).
link(a,b).
link(a,c).
link(b,d).
link(b,e).
link(c,e).
link(c,f).
link(f,g).
link(e,g).
link(g,i).
link(h,i).

rock(a0). in(a0,a).
rock(a1). in(a1,a). treasure(a1).
rock(a2). in(a2,a). treasure(a2).
rock(b0). in(b0,b).
rock(b1). in(b1,b).
rock(c0). in(c0,c).
rock(space_canary_corpse). in(space_canary_corpse,c). treasure(space_canary_corpse).
rock(d0). in(d0,d). treasure(d0).
rock(d1). in(d1,d). treasure(d1).
rock(d2). in(d2,d).
rock(scrap). in(scrap,e).
rock(robot_arm). in(robot_arm,e). treasure(robot_arm).
rock(g0). in(g0,g).
rock(h0). in(h0,h).
rock(h1). in(h1,h).
rock(i0). in(i0,i).
rock(i1). in(i1,i). treasure(i1).
rock(i2). in(i0,i). treasure(i2).
rock(i3). in(i1,i).
rock(dino_bones). in(dino_bones,i). treasure(dino_bones).


%%%%
%%%% RULE CONSTANTS
%%%%

level(0).
level(1).
level(2).
level(3).
level(4).
level(5).
level(6).
level(7).
level(8).
level(9).
level(10).
max_energy(10).

%%%%
%%%% UI BINDING
%%%%

ui_title('DrillBot 6000').
ui_instruction('DrillBot 6000 (aka "You Have to Mine the Ore") \n\
\n\
INSTRUCTIONS:\n\
- you control mining robot "db6k"\n\
- left-click ROCKS to mine them [requires energy]\n\
- left-click empty CAVERNS to enter them [moving up requires energy]\n\
- left-click the BASE to trade and refuel when docked\n\
- right-drag elements (like these instructions) to move them out of the way\n\
  (this does not affect the game world; only visual)\n\
- edit "script.pl" to modify the game\n\
\n\
Your energy level drops over time so remember to refuel often!').
ui_soundtrack('rsmy-db6k.mp3').

ui_ticker(1920).

ui_triggers(ui_tick(_),drain).

ui_space(P) :- pos(P).
ui_space(inventory).
ui_space(bank).
ui_space(slag_pile).

ui_token(db6k).
ui_token(R) :- rock(R).

ui_location(db6k,P) :- holds(position(P)).
ui_location(R,P) :- in(R,P), holds(present(R)).
ui_location(R,inventory) :- holds(bagged(R)).
ui_location(R,bank) :- holds(banked(R)).
ui_location(R,slag_pile) :- rock(R), \+ treasure(R), \+ holds(bagged(R)), \+ holds(present(R)).

defeat_at(P) :- holds(position(P)), \+ possible(_).
ui_location('...you are buried...',P) :- defeat_at(P).
ui_location('...a forgotten robot...',P) :- defeat_at(P).

ui_triggers(ui_click_space(P),down_to(P)).
ui_triggers(ui_click_space(P),up_to(P)).
ui_triggers(ui_click_space(base),trade).
ui_triggers(ui_click_space(base),refuel).

ui_triggers(ui_click_token(R),mine(R)).

ui_path(P1,P2) :- link(P1,P2).

ui_details(energy(L)) :- holds(energy(L)).

ui_details(points(P)) :- (setof(T,holds(banked(T)),Ts) -> true ; Ts=[]), length(Ts,P).
ui_details('NO ENERGY!') :- \+ energized.
ui_details('NO POSSIBLE MOVES!') :- \+ possible(_).

% read this map sidewayz
% +-----------------+
% |         d       | 
% |       b   h     |
% |base a   e   i   |
% |       c   g     |
% |         f       |
% +-----------------+


ui_layout(base,0.4875,0.15).
ui_layout(bank,0.7,0.3).
ui_layout(inventory,0.1,0.3).
ui_layout(slag_pile,0.9,0.3).

ui_layout(a,0.5,0.3).
ui_layout(b,0.4,0.45).
ui_layout(c,0.6,0.45).
ui_layout(d,0.3,0.6).
ui_layout(e,0.5,0.6).
ui_layout(f,0.7,0.6).
ui_layout(g,0.6,0.75).
ui_layout(h,0.8,0.75).
ui_layout(i,0.7,0.9).
