# 6A - 19090054 - Dwi Indah Fitria Sari 
# 6D - 19090130 - Novita Fitria Putri


from imp import load_module
import numpy as np
import keras
import datetime 
from datetime import date
from keras.models import Sequential
from keras.layers import Dense,Conv2D,MaxPool2D,Dropout,BatchNormalization,Flatten,Activation
from keras.preprocessing import image 
from keras.preprocessing.image import ImageDataGenerator
import matplotlib.pyplot as plt
from keras.utils.vis_utils import plot_model
import pickle
from flask import Flask, jsonify,request,flash,redirect,render_template, session,url_for

from itsdangerous import json
from werkzeug.utils import secure_filename
import os
from flask_cors import CORS
from flask_restful import Resource, Api
import pymongo
import re
from PIL import Image
import datetime
import random
import string

app = Flask(__name__)

UPLOAD_FOLDER = 'fotoapel'
ALLOWED_EXTENSIONS = set(['png', 'jpg', 'jpeg'])
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

app.secret_key = "bp"

MONGO_ADDR = 'mongodb://localhost:27017'
MONGO_DB = "bigproject"

conn = pymongo.MongoClient(MONGO_ADDR)
db = conn[MONGO_DB]

api = Api(app)
CORS(app)

from tensorflow.keras.models import load_model

from keras.preprocessing import image
MODEL_PATH = 'modelapel.h5'
model = load_model (MODEL_PATH,compile=False)

pickle_inn = open('num_class_apel.pkl','rb')
num_classes_apel = pickle.load(pickle_inn)

def allowed_file(filename):     
  return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

class index(Resource):
  def post(self):

    if 'image' not in request.files:
      flash('No file part')
      return jsonify({
            "pesan":"tidak ada form image"
          })
    file = request.files['image']
    if file.filename == '':
      return jsonify({
            "pesan":"tidak ada file image yang dipilih"
          })
    if file and allowed_file(file.filename):
      path_del = r"fotoapel\\"
      for file_name in os.listdir(path_del):
      
        file_del = path_del + file_name
        if os.path.isfile(file_del):
            print('Deleting file:', file_del)
            os.remove(file_del)
            print("file "+file_del+" telah terhapus")
      filename = secure_filename(file.filename)
      file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
      path=("fotoapel/"+filename)

      today = date.today()
      db.riwayat.insert_one({'nama_file': filename, 'path': path, 'prediksi':'No predict', 'akurasi':0, 'tanggal':today.strftime("%d/%m/%Y")})

    

      img=image.load_img(path,target_size=(224,224))
      img1=image.img_to_array(img)
      img1=img1/255
      img1=np.expand_dims(img1,[0])
      plt.imshow(img)
      predict=model.predict(img1)
      classes=np.argmax(predict,axis=1)
      for key,values in num_classes_apel.items():
          if classes==values:
            accuracy = float(round(np.max(model.predict(img1))*100,2))
            info = db['deskripsi'].find_one({'Deskripsi': str(key)})
            db.riwayat.update_one({'nama_file': filename}, 
              {"$set": {
                'prediksi': str(key), 
                'akurasi':accuracy
              }
              })
            if accuracy >35:
              print("The predicted image of the apple is: "+str(key)+" with a probability of "+str(accuracy)+"%")
        
              return jsonify({
                "Nama":str(key),
                "Accuracy":str(accuracy)+"%",
                "Deskripsi": info['Deskripsi'],
                "Ciri" : info['Ciri']
              })
            else :
              print("The predicted image of the apple is: "+str(key)+" with a probability of "+str(accuracy)+"%")
              return jsonify({
                "Message":str("Gambar salah "),
                "Accuracy":str(accuracy)+"%"               
                
              })
      
    else:
      return jsonify({
        "Message":"bukan file image"
      })

@app.route('/admin')
def admin():
    return render_template("login.html")
@app.route('/login', methods=['GET','POST'])
def login():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password'] 
        user = db['admin'].find_one({'username': str(username)})
        print(user)

        if user is not None and len(user) > 0:
            if password == user['password']:
                
                session['username'] = user['username']
                return redirect(url_for('dataApel'))
            else:
                return redirect(url_for('login'))
        else:
            return redirect(url_for('login'))
    else:
        return render_template('login.html')
    
    return render_template('dashboard.html')

@app.route('/dataApel')
def dataApel():
    data = db['deskripsi'].find({})
    print(data)
    return render_template('dataApel.html',dataApel  = data)

@app.route('/tambahData')
def tambahData():

    return render_template('tambahData.html')


@app.route('/daftarApel', methods=["POST"])
def daftarApel():
    if request.method == "POST":
        Nama = request.form['Nama']
        Deskripsi = request.form['Deskripsi']
        Ciri = request.form['Ciri']
       
        if not re.match(r'[A-Za-z]+', Nama):
            flash("Nama harus pakai huruf Dong!")
        
        else:
            db.deskripsi.insert_one({'Nama': Nama, 'Deskripsi': Deskripsi, 'Ciri':Ciri})
            flash('Data Apel berhasil ditambah')
            return redirect(url_for('dataApel'))

    return render_template("tambahData.html")

@app.route('/editApel/<nama>', methods = ['POST', 'GET'])
def editApel(nama):
  
    data = db['deskripsi'].find_one({'nama': nama})
    print(data)
    return render_template('editApel.html', editApel = data)


@app.route('/updateApel/<nama>', methods=['POST'])
def updatApel(nama):
    if request.method == 'POST':
        Nama = request.form['Nama']
        Deskripsi = request.form['Deskripsi']
        Ciri = request.form['Ciri']
        if not re.match(r'[A-Za-z]+', nama):
            flash("Nama harus pakai huruf Dong!")
        else:
          db.deskripsi.update_one({'nama': nama}, 
          {"$set": {
            'Nama': Nama, 
            'Deskripsi': Deskripsi, 
            'Ciri':Ciri
            }
            })

          flash('Data berhasil diupdate')
          return render_template("popUpEdit.html")

    return render_template("dataApel.html")


@app.route('/hapusApel/<nama>', methods = ['POST','GET'])
def hapusApel(nama):
  
    db.deskripsi.delete_one({'nama': nama})
    flash(' Berhasil Dihapus!')
    return redirect(url_for('dataApel'))

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('login'))


@app.route('/riwayat')
def riwayat():
    dataRiwayat = db['riwayat'].find({})
    print(dataRiwayat)
    return render_template('riwayat.html',riwayat  = dataRiwayat)
    


api.add_resource(index, "/api/image", methods=["POST"])

if __name__ == '__main__':
  

  app.run(debug = True, port=5000, host='0.0.0.0')

