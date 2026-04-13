import os
import sys
import requests
import qdarkstyle
from requests.exceptions import HTTPError
from PyQt5.QtWidgets import QWidget, QApplication, QFileDialog, QMessageBox, QPushButton
from PyQt5 import QtCore
from PyQt5.uic import loadUi

def debug_trace(ui=None):
    from pdb import set_trace
    QtCore.pyqtRemoveInputHook()
    set_trace()
    # QtCore.pyqtRestoreInputHook()


class LibraryApp(QWidget):
    ROOT_DIR = os.path.dirname(os.path.abspath(__file__))
    search_btn: QPushButton
    
    def __init__(self):
        super(LibraryApp, self).__init__()
        ui_path = os.path.join(self.ROOT_DIR, 'library_manager.ui')
        loadUi(ui_path, self)
        self.search_btn.clicked.connect(self.search)
        self.save_as_file_btn.clicked.connect(self.save_as_file)

    def search(self):
        search_string = self.search_bar.text()
        url = None
        if not search_string:
            if self.json_rb.isChecked():
                url = '/print?format=json'
            elif self.html_rb.isChecked():
                url = '/print?format=html'
            else:
                url = '/print?format=raw'
        else:
            if self.author_rb.isChecked():
                url = '/find?author={}'.format(search_string.replace(' ', '%20'))
            elif self.title_rb.isChecked():
                url = '/find?title={}'.format(search_string.replace(' ', '%20'))
            else:
                url = '/find?publisher={}'.format(search_string.replace(' ', '%20'))
        full_url = "http://localhost:8080" + url
        try:
            response = requests.get(full_url)
            self.result.setText(response.content.decode('utf-8'))
        except HTTPError as http_err:
            print('HTTP error occurred: {}'.format(http_err))
        except Exception as err:
            print('Other error occurred: {}'.format(err))

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
