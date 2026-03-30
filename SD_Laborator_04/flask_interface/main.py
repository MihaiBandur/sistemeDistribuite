from  flask import  Flask, render_template, request, redirect, url_for, session, flash
import  requests

app = Flask(__name__)
app.secret_key = 'flask_secret_key'

SPRING_BASE_URL = "http://localhost:2030"

@app.route('/')
def index():
    expenses = []
    if "user_id" in session:
        try:
            resp = requests.get(f"{SPRING_BASE_URL}/expenses", params={"userId": session['user_id']})
            if resp.status_code == 200:
                expenses = resp.json()
        except requests.exceptions.ConnectionError:
            flash("Nu se poate conecta la backend.")
    return render_template('index.html', expenses=expenses)


@app.route('/register', methods=['POST'])
def register():
    payload = {
        "username": request.form['username'],
        "password": request.form['password'],
        "firstName": request.form['firstName'],
        "lastName": request.form['lastName']
    }
    try:
        resp = requests.post(f"{SPRING_BASE_URL}/users/register", json=payload)
        if resp.status_code == 200:
            flash("Cont creat cu succes! Te poti loga acum.")
        else:
            data = resp.json()
            flash(f"Eroare: {data.get('error', 'Necunoscuta')}")
    except requests.exceptions.ConnectionError:
        flash("Nu s-a conectat la backedn")
    return redirect(url_for('index'))

@app.route('/login', methods=['POST'])
def login():
    payload = {
        "username": request.form['username'],
        "password": request.form['password']
    }

    try:
        resp = requests.post(f"{SPRING_BASE_URL}/users/login", json=payload)
        if resp.status_code == 200:
            data = resp.json()
            session['username'] = data['username']
            session['user_id'] = data['id']
            flash("Te-ai logat cu succes!")
        else:
            err = resp.json()
            flash(f"Eroare autentificare: {err.get('error', 'Date incorecte')}")
    except requests.exceptions.ConnectionError:
        flash("Nu se poate conecta la backend.")
    return redirect(url_for('index'))


@app.route('/add_expense', methods=['POST'])
def add_expense():
    if 'user_id' not in session:
        return redirect(url_for('index'))

    payload = {
        "userId": session['user_id'],
        "amount": float(request.form['amount']),
        "category": request.form['category'],
        "description": request.form['description']
    }
    try:
        resp = requests.post(f"{SPRING_BASE_URL}/expenses", json=payload)
        if resp.status_code == 201:
            flash(f"Cheltuiala adăugata: {payload['amount']} RON ({payload['category']})")
        else:
            # Afișează eroarea exactă de la Spring Boot
            flash(f"Eroare {resp.status_code}: {resp.text}")
    except requests.exceptions.ConnectionError:
        flash("Nu se poate conecta la backend.")
    return redirect(url_for('index'))
@app.route('/logout')
def logout():
    session.pop('username', None)
    session.pop('user_id', None)
    flash("Te-ai delogat")
    return redirect(url_for('index'))

if __name__ == '__main__':
    app.run(port=8000, debug=True)