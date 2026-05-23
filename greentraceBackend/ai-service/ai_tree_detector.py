from flask import Flask, request, jsonify
import requests
import cv2
import numpy as np
from deepforest import main

app = Flask(__name__)

# ===================================
# LOAD DEEPFOREST MODEL
# ===================================

model = main.deepforest()
model.load_model()


# ===================================
# DOWNLOAD SATELLITE IMAGE
# ===================================
def download_satellite_image(lat, lon):

    zoom = 18

    xtile = int((lon + 180) / 360 * (2 ** zoom))
    ytile = int(
        (1 - np.log(np.tan(np.radians(lat)) + 1 / np.cos(np.radians(lat))) / np.pi)
        / 2
        * (2 ** zoom)
    )

    url = f"https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{zoom}/{ytile}/{xtile}"

    try:
        r = requests.get(url, timeout=10)

        img_array = np.frombuffer(r.content, np.uint8)
        img = cv2.imdecode(img_array, cv2.IMREAD_COLOR)

        if img is None:
            img = np.zeros((256,256,3), dtype=np.uint8)

    except:
        img = np.zeros((256,256,3), dtype=np.uint8)

    # upscale image for detection
    img = cv2.resize(img, (1024,1024))

    return img


# ===================================
# TREE DETECTION ENDPOINT
# ===================================

@app.route("/detect-trees", methods=["POST"])
def detect_trees():

    data = request.json

    lat = data["latitude"]
    lon = data["longitude"]

    try:

        image = download_satellite_image(lat, lon)

        # convert image type to avoid warning
        image = image.astype("float32")

        predictions = model.predict_image(image=image)

        tree_count = 0

        if predictions is not None:
            try:
                tree_count = len(predictions)
            except:
                tree_count = 0

        return jsonify({
            "detectedTrees": int(tree_count)
        })

    except Exception as e:

        print("Tree detection error:", e)

        return jsonify({
            "detectedTrees": 0
        })


# ===================================
# START SERVER
# ===================================

if __name__ == "__main__":
    app.run(port=5001)
