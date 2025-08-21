import os
from PIL import Image


images_dir = r"D:\img_processing_trial\img_processing_trial\vending_dataset_for_kitkat\images\val"   
labels_dir = r"D:\img_processing_trial\img_processing_trial\vending_dataset_for_kitkat\labels\val"  
def add_bbox(image_name, class_id):
    label_name = os.path.splitext(image_name)[0] + ".txt"
    label_path = os.path.join(labels_dir, label_name)
    yolo_line = f"{class_id} 0.5 0.5 1.0 1.0\n"
    with open(label_path, "a") as f: 
         f.write(yolo_line)

    print(f"✅ Added bbox to {label_path}")


for x in os.listdir(images_dir):
    if x.endswith('.jpg') or x.endswith('.png'):
        print(f"Processing {x}...")
        add_bbox(x, 0)  