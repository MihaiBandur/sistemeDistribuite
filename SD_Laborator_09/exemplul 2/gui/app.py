from flask import Flask, render_template, request, redirect, url_for
import requests

app = Flask(__name__)


CLIENT_MICROSERVICE_URL = "http://localhost:8081/api/client"

@app.route('/')
def index():
    produse = [
        "Masca protectie",
        "Vaccin anti-COVID-19",
        "Combinezon",
        "Manusa chirurgicala"
    ]
    return render_template('index.html', produse=produse)

@app.route('/client', methods=['POST'])
def trimite_comanda():
    nume = request.form.get('nume')
    adresa = request.form.get('adresa')
    produs = request.form.get('produs')
    cantitate = request.form.get('cantitate')

    if nume and adresa and produs and cantitate:
        mesaj = f"{nume}|{produs}|{cantitate}|{adresa}"
        try:
            requests.post(CLIENT_MICROSERVICE_URL, data=mesaj)
            print(f"Comanda trimisa: {mesaj}")
        except Exception as e:
            print(f"Eroare: Nu m-am putut conecta la Java. Verifica daca ruleaza: {e}")

    return redirect(url_for('index'))

if __name__ == '__main__':
    app.run(debug=True, port=5000)