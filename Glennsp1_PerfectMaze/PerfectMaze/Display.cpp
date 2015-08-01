#include "stdafx.h"
#include "Display.h"

Display::Display(Graphics^ graphics) {
	gfx = graphics;
	config = gcnew Config();
}

void Display::DrawMaze(array<Node^,2>^ maze) {
	maze[0,0]->Visited = true;
	for (int i = 0; i < config->MAX_COLS; i++) {
		for (int j = 0; j < config->MAX_ROWS; j++) {
			Point cellAddress = Point(i, j);
			Node^ currentNode = maze[i, j];
			drawCell(cellAddress, currentNode);
		}
	}
}



void Display::DrawSolution(Queue<Node^>^ path) {
	while(!path->IsEmpty()) {
		Node^ currentNode = path->Pop();
		Point ca = currentNode->Address;
		gfx->FillRectangle(gcnew SolidBrush(Color::LightGreen), ca.X * config->CELL_SIZE + 1,
																ca.Y * config->CELL_SIZE + 1,
																config->CELL_SIZE - 1,
																config->CELL_SIZE - 1);
	}
}

void Display::ClearScreen() {
	gfx->Clear(Color::White);
}

void Display::drawCell(Point cellAddress, Node^ node) {
	Pen^ p = gcnew Pen(Color::Black);

	int sX = cellAddress.X * config->CELL_SIZE;
	int sY = cellAddress.Y * config->CELL_SIZE;
	

	if (node->Visited) {
		gfx->FillRectangle(gcnew SolidBrush(Color::LightBlue), sX, sY, config->CELL_SIZE, config->CELL_SIZE);
	}

	// Draws North boundary to the screen
	if (node->North) {
		int x2 = sX + config->CELL_SIZE;
		gfx->DrawLine(p, sX, sY, x2, sY);
	}
	// Draws East boundary to the screen
	if (node->East) {
		int x = sX + config->CELL_SIZE;
		int y2 = sY + config->CELL_SIZE;

		gfx->DrawLine(p, x, sY, x, y2);
	}

	// Draws South boundary to the screen
	if (node->South) {
		int x2 = sX + config->CELL_SIZE;
		int y = sY + config->CELL_SIZE;

		gfx->DrawLine(p, sX, y, x2, y);
	}
	// Draws West boundary to the screen
	if (node->West) {
		int y2 = sY + config->CELL_SIZE;
		gfx->DrawLine(p, sX, sY, sX, y2);
	}
}