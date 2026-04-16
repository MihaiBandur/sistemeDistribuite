import os
import sys
import requests
import qdarkstyle
from requests.exceptions import HTTPError
from PyQt6.QtWidgets import QWidget, QApplication, QFileDialog, QMessageBox, QPushButton, QDialog, QVBoxLayout, QLabel, \
    QLineEdit, QDialogButtonBox
from PyQt6 import QtCore
from PyQt6.uic import loadUi

def debug_trace(ui=None):
    from pdb import set_trace
    QtCore.pyqtRemoveInputHook()
    set_trace()
    # QtCore.pyqtRestoreInputHook()

class AddBookDialog(QDialog):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.setWindowTitle("Adaugă o carte nouă")
        self.resize(300, 250)
        self.layout = QVBoxLayout(self)

        self.layout.addWidget(QLabel("Titlu:"))
        self.title_input = QLineEdit(self)
        self.layout.addWidget(self.title_input)

        self.layout.addWidget(QLabel("Autor:"))
        self.author_input = QLineEdit(self)
        self.layout.addWidget(self.author_input)

        self.layout.addWidget(QLabel("Editura:"))
        self.pub_input = QLineEdit(self)
        self.layout.addWidget(self.pub_input)

        self.layout.addWidget(QLabel("Text / Conținut:"))
        self.text_input = QLineEdit(self)
        self.layout.addWidget(self.text_input)

        # Butoanele standard OK / Cancel
        self.buttons = QDialogButtonBox(
            QDialogButtonBox.StandardButton.Ok | QDialogButtonBox.StandardButton.Cancel,
            self
        )
        self.buttons.accepted.connect(self.accept)
        self.buttons.rejected.connect(self.reject)
        self.layout.addWidget(self.buttons)

    def get_data(self):
        # Returnează datele introduse sub formă de dicționar
        return {
            "title": self.title_input.text().strip(),
            "author": self.author_input.text().strip(),
            "publisher": self.pub_input.text().strip(),
            "text": self.text_input.text().strip()
        }
class LibraryApp(QWidget):
    ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
    search_btn: QPushButton
    add_book_btn: QPushButton
    
    def __init__(self):
        super(LibraryApp, self).__init__()
        ui_path = os.path.join(self.ROOT_DIR, 'library_manager.ui')
        loadUi(ui_path, self)

        self.search_btn.clicked.connect(self.search)
        self.save_as_file_btn.clicked.connect(self.save_as_file)
        self.add_book_btn.clicked.connect(self.open_add_dialog)

    def open_add_dialog(self):
        # Deschide fereastra de adăugare
        dialog = AddBookDialog(self)
        if dialog.exec() == QDialog.DialogCode.Accepted:
            data = dialog.get_data()

            # Validăm ca toate câmpurile să fie completate
            if not all(data.values()):
                QMessageBox.warning(self, "Eroare", "Toate câmpurile sunt obligatorii!")
                return

            # Trimitem datele către serverul Spring Boot prin HTTP
            # Folosim parametrul `params` al librăriei requests pentru a atașa dicționarul la URL
            url = "http://localhost:8080/add"
            try:
                response = requests.get(url, params=data)
                response.raise_for_status()  # Verifică dacă răspunsul este OK (200)
                QMessageBox.information(self, "Succes", response.text)

                # Reîmprospătăm automat lista după adăugare
                self.search()

            except HTTPError as http_err:
                QMessageBox.warning(self, "Eroare Server", f"A apărut o problemă: {http_err}")
            except Exception as err:
                QMessageBox.critical(self, "Eroare Conexiune",
                                     "Nu s-a putut conecta la server. Rulează aplicația Kotlin!")

    def search(self):
        search_string = self.search_bar.text()

        # 1. Stabilim formatul
        format_type = "raw"
        if self.json_rb.isChecked():
            format_type = "json"
        elif self.html_rb.isChecked():
            format_type = "html"


        if not search_string:

            url = f'/find-and-print?format={format_type}'
        else:
            # Dacă avem text, vedem ce criteriu este selectat
            if self.author_rb.isChecked():
                criterion = "author"
            elif self.title_rb.isChecked():
                criterion = "title"
            else:
                criterion = "publisher"


            formatted_search = search_string.replace(' ', '%20')
            url = f'/find-and-print?{criterion}={formatted_search}&format={format_type}'

        full_url = "http://localhost:8080" + url


        try:
            response = requests.get(full_url)
            self.result.setText(response.content.decode('utf-8'))
        except HTTPError as http_err:
            print(f'HTTP error occurred: {http_err}')
        except Exception as err:
            print(f'Other error occurred: {err}')
            self.result.setText("Eroare de conexiune. Asigură-te că serverul Spring Boot rulează pe portul 8080.")

    def save_as_file(self):
        options = QFileDialog.Options()
        options |= QFileDialog.DontUseNativeDialog
        file_path = str(
            QFileDialog.getSaveFileName(self,
                                        'Salvare fisier',
                                        options=options))
        if file_path:
            file_path = file_path.split("'")[1]
            if not file_path.endswith('.json') and not file_path.endswith(
                    '.html') and not file_path.endswith('.txt'):
                if self.json_rb.isChecked():
                    file_path += '.json'
                elif self.html_rb.isChecked():
                    file_path += '.html'
                else:
                    file_path += '.txt'
            try:
                with open(file_path, 'w') as fp:
                    if file_path.endswith(".html"):
                        fp.write(self.result.toHtml())
                    else:
                        fp.write(self.result.toPlainText())
            except Exception as e:
                print(e)
                QMessageBox.warning(self, 'Library Manager',
                                    'Nu s-a putut salva fisierul')


if __name__ == '__main__':
    app = QApplication(sys.argv)

    stylesheet = qdarkstyle.load_stylesheet_pyqt5()
    app.setStyleSheet(stylesheet)

    window = LibraryApp()
    window.show()
    sys.exit(app.exec_())
