import sys
import socket
import threading

from PyQt6.QtWidgets import (
    QApplication, QWidget, QLabel,
    QLineEdit, QTextEdit, QPushButton,
    QHBoxLayout, QVBoxLayout
)
from PyQt6.QtCore import pyqtSignal, QObject

HOST = "localhost"
TEACHER_PORT = 1600


class Worker(QObject):
    response_received = pyqtSignal(str)

    def __init__(self):
        super().__init__()
        self.sock = None
        self._connect()

    def _connect(self):
        try:
            self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.sock.connect((HOST, TEACHER_PORT))
            t = threading.Thread(target=self._listen, daemon=True)
            t.start()
        except Exception as e:
            self.response_received.emit(f"Eroare conectare la Teacher: {e}")
            self.sock = None

    def _listen(self):
        try:
            buffer = ""
            while True:
                data = self.sock.recv(1024)
                if not data:
                    break
                buffer += data.decode("utf-8")
                # Proceseaza fiecare linie completa
                while "\n" in buffer:
                    line, buffer = buffer.split("\n", 1)
                    line = line.strip()
                    if line:
                        self.response_received.emit(line)
        except Exception as e:
            self.response_received.emit(f"Conexiune pierduta: {e}")

    def send_question(self, question_text):

        if self.sock:
            try:
                self.sock.send((question_text + "\n").encode("utf-8"))
            except Exception as e:
                self.response_received.emit(f"Eroare la trimitere: {e}")
        else:
            self.response_received.emit("Nu esti conectat la Teacher!")


class MainWindow(QWidget):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Interactiune profesor-studenti")
        self.setMinimumSize(500, 200)
        self.resize(600, 250)

        self.worker = Worker()
        self.worker.response_received.connect(self.on_response)

        self.response_widget = QTextEdit()
        self.response_widget.setReadOnly(True)

        self.question_label = QLabel("Profesorul intreaba:")
        self.question_input = QLineEdit()
        self.question_input.setPlaceholderText("Scrie intrebarea aici...")
        self.question_input.returnPressed.connect(self.ask_question)

        self.ask_button = QPushButton("Intreaba")
        self.ask_button.clicked.connect(self.ask_question)

        self.exit_button = QPushButton("Iesi")
        self.exit_button.clicked.connect(self.close)

        right_layout = QVBoxLayout()
        right_layout.addWidget(self.question_label)
        right_layout.addWidget(self.question_input)
        right_layout.addStretch()

        btn_layout = QHBoxLayout()
        btn_layout.addStretch()
        btn_layout.addWidget(self.ask_button)
        btn_layout.addWidget(self.exit_button)
        right_layout.addLayout(btn_layout)

        main_layout = QHBoxLayout()
        main_layout.addWidget(self.response_widget, stretch=2)
        main_layout.addLayout(right_layout, stretch=1)

        self.setLayout(main_layout)

    def ask_question(self):
        question_text = self.question_input.text().strip()
        if not question_text:
            return
        self.question_input.clear()

        t = threading.Thread(
            target=self.worker.send_question,
            args=(question_text,),
            daemon=True
        )
        t.start()

    def on_response(self, text):
        self.response_widget.append(text)


if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = MainWindow()
    window.show()
    sys.exit(app.exec())