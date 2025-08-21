from ultralytics import YOLO

# Load a pre-trained YOLOv8s model
model = YOLO("yolov8n.pt")

# Train the model
model.train(
    data=r"D:\img_processing_trial\img_processing_trial\vending_dataset_for_kitkat\data.yaml",   # path to your dataset yaml
    epochs=15,               # number of epochs
    imgsz=416,               # image size
    device='cpu',
    batch=8,
    augment=False,
    mosaic=0.0
               
)