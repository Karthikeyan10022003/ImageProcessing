import os
def check_labels_in_dir(labels_dir):
    print(f"\n🔍 Checking: {labels_dir}")
    for filename in os.listdir(labels_dir):
        if filename.lower().endswith('.txt'):
            label_path = os.path.join(labels_dir, filename)
            with open(label_path, 'r') as file:
                lines = file.readlines()
                for line in lines:
                    values = line.strip().split()
                    if len(values) != 5:
                        print(f"❌ Invalid format in {label_path}: {line.strip()}")
                        continue
                    class_id, x_center, y_center, width, height = values
                    try:
                        if not (class_id.isdigit() and 0 <= int(class_id) < 1000):
                            print(f"❌ Invalid class ID in {label_path}: {class_id}")
                        if not (0 <= float(x_center) <= 1 and 
                                0 <= float(y_center) <= 1 and 
                                0 <= float(width) <= 1 and 
                                0 <= float(height) <= 1):
                            print(f"❌ Out of bounds in {label_path}: {line.strip()}")
                    except ValueError:
                        print(f"❌ Non-numeric values in {label_path}: {line.strip()}")
    print("✅ Finished checking folder.")

# Paths to train and val labels
train_labels = r"D:\img_processing_trial\img_processing_trial\vending_dataset_for_kitkat\labels\train"
val_labels   = r"D:\img_processing_trial\img_processing_trial\vending_dataset_for_kitkat\labels\val"
# Run checks
check_labels_in_dir(train_labels)
check_labels_in_dir(val_labels)