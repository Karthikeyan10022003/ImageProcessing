import os
from PIL import Image

# ==== CONFIG ====
images_dir = r"D:\img_processing_trial\vending_dataset\images\val"   # Path to your images
labels_dir = r"D:\img_processing_trial\vending_dataset\labels\val"   # Path to your label files

def add_bbox(image_name, class_id, x_min, y_min, x_max, y_max):
    # Get image size
    image_path = os.path.join(images_dir, image_name)
    if not os.path.exists(image_path):
        print(f"❌ Image not found: {image_path}")
        return

    with Image.open(image_path) as img:
        img_width, img_height = img.size

    # Convert to YOLO format
    x_center = ((x_min + x_max) / 2) / img_width
    y_center = ((y_min + y_max) / 2) / img_height
    width = (x_max - x_min) / img_width
    height = (y_max - y_min) / img_height

    # Write to label file
    label_name = os.path.splitext(image_name)[0] + ".txt"
    label_path = os.path.join(labels_dir, label_name)

    with open(label_path, "a") as f:  # Append in case multiple objects
        f.write(f"{class_id} {x_center:.6f} {y_center:.6f} {width:.6f} {height:.6f}\n")

    print(f"✅ Added bbox to {label_path}")

# ==== Example Usage ====
# Change these values for each object you want to label
# add_bbox("example.jpg", 0, 100, 150, 300, 350)  # (x_min, y_min, x_max, y_max)
for x in os.listdir(images_dir):
    if x.endswith('.jpg') or x.endswith('.png'):
        print(f"Processing {x}...")
        add_bbox(x, 0, 100, 150, 300, 350)  # Example values, replace with actual coordinates