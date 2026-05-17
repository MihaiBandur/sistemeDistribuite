import sqlite3
import os
import shutil

safari_db_path = os.path.expanduser('~/Library/Safari/History.db')

if os.path.exists(safari_db_path):
    print(f"S-a gasit baza de date la {safari_db_path}")

    try:
        copy_name = 'safari_history_copy.db'
        shutil.copyfile(safari_db_path, copy_name)
        conn = sqlite3.Connection(copy_name)

        cursor = conn.cursor()

        query = """
        SELECT url, visit_count 
        FROM history_items 
        WHERE visit_count > 0
        """

        cursor.execute(query)

        with open("browser_history.txt", "+w", encoding='utf-8') as f:
            for url, count in cursor.fetchall():
                f.write(f"{url}\t{count}\n")
        
        conn.close()
        os.remove(copy_name)
    
        print("Operatia de copiere din baza de data a browser-ului este incheiata cu succes")

    except Exception as e:
        print(f"Problema la baza de data{e}")

else:
    print("Este posibil ca baza de date sa nu existe")


