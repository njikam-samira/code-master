all: ED CC CCen BC EV NC Community Rolx AS HS PR Dist

Dist:
	g++ -std=c++98 -Wall -O3 -DNDEBUG -o Distance Distance.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

ED:
	g++ -std=c++98 -Wall -O3 -DNDEBUG -o ED ED.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

CC:
	g++ -std=c++98 -Wall -O3 -DNDEBUG -o CC CC.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

CCen:
	g++ -std=c++98 -Wall -O3 -DNDEBUG -o CCen CCen.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

BC:
	g++ -std=c++98 -Wall -O3 -DNDEBUG -o BC BC.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

EV:
	g++ -std=c++98 -Wall -O3 -DNDEBUG -o EV EV.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

NC:
	g++ -std=c++98 -Wall -O3 -DNDEBUG -o NC NC.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

AS:
	g++ -std=c++98 -Wall -O3 -DNDEBUG -o AS AS.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

HS:
	g++ -std=c++98 -Wall -O3 -DNDEBUG -o HS HS.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

PR:
	g++ -std=c++98 -Wall -O3 -DNDEBUG -o PR PR.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

Community:
	g++ -std=c++98 -Wall -O3 -DNDEBUG -o Community community/community.cpp  ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

Rolx:
	g++ -fopenmp -o Rolx rolx/testrolx.cpp ../../snap-adv/rolx.cpp ../../snap-core/Snap.o -I../../snap-core -I../../snap-adv -I../../glib-core -I../../snap-exp

clean:
	del /Q *.o *.exe