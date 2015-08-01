#pragma once

#include "Manager.h"

namespace PerfectMaze {

	using namespace System;
	using namespace System::ComponentModel;
	using namespace System::Collections;
	using namespace System::Windows::Forms;
	using namespace System::Data;
	using namespace System::Drawing;

	/// <summary>
	/// Summary for Form1
	///
	/// WARNING: If you change the name of this class, you will need to change the
	///          'Resource File Name' property for the managed resource compiler tool
	///          associated with all .resx files this class depends on.  Otherwise,
	///          the designers will not be able to interact properly with localized
	///          resources associated with this form.
	/// </summary>
	public ref class Form1 : public System::Windows::Forms::Form
	{
	public:
		Form1(void)
		{
			InitializeComponent();
			//
			//TODO: Add the constructor code here
			//
		}

	protected:
		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		~Form1()
		{
			if (components)
			{
				delete components;
			}
		}

	private:
		/// <summary>
		/// Required designer variable.
		/// </summary>
		System::ComponentModel::Container ^components;
	private: System::Windows::Forms::Panel^  displayPanel;
	private: System::Windows::Forms::Button^  button1;
	private: System::Windows::Forms::Button^  button2;
	private: System::Windows::Forms::GroupBox^  groupBox1;
	private: System::Windows::Forms::TextBox^  rowInput;

	private: System::Windows::Forms::Label^  label1;
	private: System::Windows::Forms::TextBox^  textBox2;
	private: System::Windows::Forms::Label^  colInput;
	private: System::Windows::Forms::Button^  applyButton;



		Manager^ manager;

#pragma region Windows Form Designer generated code
		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		void InitializeComponent(void)
		{
			this->displayPanel = (gcnew System::Windows::Forms::Panel());
			this->button1 = (gcnew System::Windows::Forms::Button());
			this->button2 = (gcnew System::Windows::Forms::Button());
			this->groupBox1 = (gcnew System::Windows::Forms::GroupBox());
			this->applyButton = (gcnew System::Windows::Forms::Button());
			this->textBox2 = (gcnew System::Windows::Forms::TextBox());
			this->colInput = (gcnew System::Windows::Forms::Label());
			this->rowInput = (gcnew System::Windows::Forms::TextBox());
			this->label1 = (gcnew System::Windows::Forms::Label());
			this->groupBox1->SuspendLayout();
			this->SuspendLayout();
			// 
			// displayPanel
			// 
			this->displayPanel->BackColor = System::Drawing::Color::White;
			this->displayPanel->Location = System::Drawing::Point(12, 61);
			this->displayPanel->Name = L"displayPanel";
			this->displayPanel->Size = System::Drawing::Size(655, 603);
			this->displayPanel->TabIndex = 0;
			// 
			// button1
			// 
			this->button1->Location = System::Drawing::Point(12, 32);
			this->button1->Name = L"button1";
			this->button1->Size = System::Drawing::Size(98, 23);
			this->button1->TabIndex = 1;
			this->button1->Text = L"Solve";
			this->button1->UseVisualStyleBackColor = true;
			this->button1->Click += gcnew System::EventHandler(this, &Form1::button1_Click);
			// 
			// button2
			// 
			this->button2->Location = System::Drawing::Point(12, 6);
			this->button2->Name = L"button2";
			this->button2->Size = System::Drawing::Size(98, 23);
			this->button2->TabIndex = 2;
			this->button2->Text = L"Create Maze";
			this->button2->UseVisualStyleBackColor = true;
			this->button2->Click += gcnew System::EventHandler(this, &Form1::button2_Click);
			// 
			// groupBox1
			// 
			this->groupBox1->Controls->Add(this->applyButton);
			this->groupBox1->Controls->Add(this->textBox2);
			this->groupBox1->Controls->Add(this->colInput);
			this->groupBox1->Controls->Add(this->rowInput);
			this->groupBox1->Controls->Add(this->label1);
			this->groupBox1->Location = System::Drawing::Point(383, 6);
			this->groupBox1->Name = L"groupBox1";
			this->groupBox1->Size = System::Drawing::Size(286, 49);
			this->groupBox1->TabIndex = 3;
			this->groupBox1->TabStop = false;
			this->groupBox1->Text = L"Config";
			// 
			// applyButton
			// 
			this->applyButton->Location = System::Drawing::Point(212, 16);
			this->applyButton->Name = L"applyButton";
			this->applyButton->Size = System::Drawing::Size(56, 23);
			this->applyButton->TabIndex = 4;
			this->applyButton->Text = L"Apply";
			this->applyButton->UseVisualStyleBackColor = true;
			this->applyButton->Click += gcnew System::EventHandler(this, &Form1::applyButton_Click);
			// 
			// textBox2
			// 
			this->textBox2->Location = System::Drawing::Point(146, 19);
			this->textBox2->MaxLength = 2;
			this->textBox2->Name = L"textBox2";
			this->textBox2->Size = System::Drawing::Size(45, 20);
			this->textBox2->TabIndex = 3;
			this->textBox2->Text = L"3";
			// 
			// colInput
			// 
			this->colInput->AutoSize = true;
			this->colInput->Location = System::Drawing::Point(110, 22);
			this->colInput->Name = L"colInput";
			this->colInput->Size = System::Drawing::Size(30, 13);
			this->colInput->TabIndex = 2;
			this->colInput->Text = L"Cols:";
			// 
			// rowInput
			// 
			this->rowInput->Location = System::Drawing::Point(50, 19);
			this->rowInput->MaxLength = 2;
			this->rowInput->Name = L"rowInput";
			this->rowInput->Size = System::Drawing::Size(45, 20);
			this->rowInput->TabIndex = 1;
			this->rowInput->Text = L"3";
			// 
			// label1
			// 
			this->label1->AutoSize = true;
			this->label1->Location = System::Drawing::Point(7, 22);
			this->label1->Name = L"label1";
			this->label1->Size = System::Drawing::Size(37, 13);
			this->label1->TabIndex = 0;
			this->label1->Text = L"Rows:";
			// 
			// Form1
			// 
			this->AutoScaleDimensions = System::Drawing::SizeF(6, 13);
			this->AutoScaleMode = System::Windows::Forms::AutoScaleMode::Font;
			this->ClientSize = System::Drawing::Size(681, 673);
			this->Controls->Add(this->groupBox1);
			this->Controls->Add(this->button1);
			this->Controls->Add(this->button2);
			this->Controls->Add(this->displayPanel);
			this->Name = L"Form1";
			this->Text = L"Maze Generator";
			this->Load += gcnew System::EventHandler(this, &Form1::Form1_Load);
			this->groupBox1->ResumeLayout(false);
			this->groupBox1->PerformLayout();
			this->ResumeLayout(false);

		}

		#pragma endregion
		private: System::Void Form1_Load(System::Object^  sender, System::EventArgs^  e) {
			Graphics^ gfx = displayPanel->CreateGraphics();
			Display^ display = gcnew Display(gfx);

			manager = gcnew Manager(display);
		}

		private: System::Void button1_Click(System::Object^  sender, System::EventArgs^  e) {
			manager->draw();
		}

		private: System::Void button2_Click(System::Object^  sender, System::EventArgs^  e) {
			manager->createMaze();
		}

		private: System::Void applyButton_Click(System::Object^  sender, System::EventArgs^  e) {

		}
	};
}

