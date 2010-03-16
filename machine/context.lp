#const max_t=14.
timepoint(0..max_t).

%:- { happens(E, Ti) : game_event(E) } 0, timepoint(Ti).

#hide.
#show happens(E,T).
