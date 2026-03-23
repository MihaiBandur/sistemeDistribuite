import os
import sys
import json
from re import search

from PyQt6.QtWidgets import (QWidget, QApplication, QFileDialog, QMessageBox,
                             QDialog, QVBoxLayout, QFormLayout, QLineEdit,
                             QTextEdit, QPushButton)
from PyQt6 import QtCore
from PyQt6.uic import loadUi
from mq_communication import RabbitMq


class AddBookDialog(QDialog):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Adauga o carte nou")
        self.resize(400, 300)

        layout = QVBoxLayout(self)
        form_layout = QFormLayout()

        self.title_input = QLineEdit()
        self.author_input = QLineEdit()
        self.publisher_input = QLineEdit()
        self.text_input = QTextEdit()

        form_layout.addRow("Titlu:", self.title_input)
        form_layout.addRow("Autor:", self.author_input)
        form_layout.addRow("Editura:", self.publisher_input)
        form_layout.addRow("Conținut:", self.text_input)

        layout.addLayout(form_layout)

        self.submit_btn = QPushButton("Adauga Cartea")
        self.submit_btn.clicked.connect(self.accept)
        layout.addWidget(self.submit_btn)

    def get_data(self):
        return {
            'title': self.title_input.text().strip(),
            'author': self.author_input.text().strip(),
            'publisher': self.publisher_input.text().strip(),
            'text': self.text_input.toPlainText().strip()
        }

def debug_trace(ui=None):
    from pdb import set_trace
    QtCore.pyqtRemoveInputHook()
    set_trace()
    # QtCore.pyqtRestoreInputHook()


class LibraryApp(QWidget):
    ROOT_DIR = os.path.dirname(os.path.abspath(__file__))

    def __init__(self):
        super(LibraryApp, self).__init__()
        ui_path = os.path.join(self.ROOT_DIR, 'exemplul_2.ui')
        loadUi(ui_path, self)

        self.search_btn.clicked.connect(self.search)
        self.save_as_file_btn.clicked.connect(self.save_as_file)

        if hasattr(self, 'add_book_btn'):
            self.add_book_btn.clicked.connect(self.open_add_book_dialog)

        self.rabbit_mq = RabbitMq(self)

    def set_response(self, response):
        self.result.setText(response)

    def send_request(self, request):
        self.rabbit_mq.send_message(message=request)
        self.rabbit_mq.receive_message()

    def search(self):
        search_string = self.search_bar.text()

        format_str = 'raw'
        if self.json_rb.isChecked():
            format_str = 'json'
        elif self.html_rb.isChecked():
            format_str = 'html'
        elif hasattr(self, 'xml_rb') and self.xml_rb.isChecked():
            format_str = 'xml'
        request = None
        if not  search_string:
            request = f'print:{format_str}'
        else:
            if self.author_rb.isChecked():
                request = f'find:author={search_string}:{format_str}'
            elif self.title_rb.isChecked():
                request = f'find:title={search_string}:{format_str}'
            else:
                request = f'find:publisher={search_string}:{format_str}'

        self.send_request(request)

    def open_add_book_dialog(self):
        dialog = AddBookDialog(self)

        if dialog.exec() == QDialog.DialogCode.Accepted:
            data = dialog.get_data()

            if not data['title'] or not data['author']:
                QMessageBox.warning(self, "Eroare", "Titlul și autorul sunt obligatorii!")
                return

            request = f"add:title={data['title']}|author={data['author']}|publisher={data['publisher']}|text={data['text']}"

            self.send_request(request)
    def save_as_file(self):
        if self.json_rb.isChecked():
            ext = ".json"
            file_filter = 'JSON Files (*.json)'
        elif self.html_rb.isChecked():
            ext = ".html"
            file_filter = 'HTML Files (*.html)'
        elif hasattr(self, 'xml_rb') and self.xml_rb.isChecked():
            ext = '.xml'
            file_filter = 'XML Files (*.xml)'
        else:
            ext = ".txt"
            file_filter = 'Text Files (*.txt)'

        file_path, _ = QFileDialog.getSaveFileName(
            self,
            'Salvare fisier',
            '',
            file_filter
        )


        if file_path:
            if not file_path.endswith(ext):
                file_path += ext
            try:
                with open(file_path, 'w', encoding='utf-8') as fp:
                    if ext == '.html':
                        fp.write(self.result.toHtml())
                    elif ext == '.json':
                        raw_text = self.result.toPlainText()
                        try:
                            parsed_json = json.loads(raw_text)
                            json.dump(parsed_json, fp, ensure_ascii=False, indent=4)
                        except json.JSONDecodeError:
                            print("Avertisment: Textul nu este un JSON perfect valid. Se salveaza brut.")
                            fp.write(raw_text)
                    else:
                        fp.write(self.result.toPlainText())
                QMessageBox.information(self, 'Succes', f'Fisierul a fost salvat cu succes ca {ext}!')
            except Exception as e:
                print(f' Eroare la salvare {e}')
                QMessageBox.warning(self, 'Eroare', 'Nu s-a putut salva fisierul.')

if __name__ == '__main__':
    app = QApplication(sys.argv)
    window = LibraryApp()
    window.show()
    sys.exit(app.exec())
