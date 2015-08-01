#pragma once
#include "Node.h"
#include "Queue.h"
#include "Config.h"

using namespace::System;

ref class MazeSolver {
private:
	Random^ rGen;
	Config^ config;
	System::Collections::Generic::List<Node^>^ retrievePossibleMoves(Node^ currentNode, array<Node^,2>^ maze);
public:
	MazeSolver(Config^ sConfig);
	Queue<Node^>^ solveMaze(array<Node^, 2>^ maze);

};