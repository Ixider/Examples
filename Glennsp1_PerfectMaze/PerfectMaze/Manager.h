#pragma once
#include "Mazegenerator.h"
#include "MazeSolver.h"
#include "Display.h"
#include "Config.h"

using namespace::System;

ref class Manager {
private:
	Display^ display;

	MazeGenerator^ mg;
	MazeSolver^ ms;

	array<Node^, 2>^ maze;
	void clearCanvas();


public:
	Manager(Display^ Display);
	void draw();
	void createMaze();
	void init(Config^ config);
};