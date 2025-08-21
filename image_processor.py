import requests
import cv2
from ultralytics import YOLO

def image_detection(model, image_path):
    try:
    # Read the image
        frame = cv2.imread(image_path)

        if frame is None:
            print(" Error: Could not read image.")
            return

        # Run detection
        results = model(frame, conf=0.1, imgsz=1280)

        # Annotate the image
        annotated_frame = results[0].plot()

        # Show the result
        cv2.imshow("Snack Detection", annotated_frame)
        cv2.waitKey(0)
        

        # Save output image
        cv2.imwrite("output_image.jpg", annotated_frame)
    except(KeyboardInterrupt):
        print("Error: Keyboard interrupt detected. Exiting.")
    finally:
        print("Image detection completed successfully.")
        cv2.destroyAllWindows()




def main():
    # Load model only once
    model = YOLO(r"D:\img_processing_trial\img_processing_trial\kitkat_lays_kurkure.pt")
    image_path=r"D:\img_processing_trial\img_processing_trial\Screenshot 2025-08-11 144949.png"
    img_link = requests.get("https://images-cdn.ubuy.co.in/63759dbdf83d7d0f39136a74-adbi-39-s-box-variety-pack-40.jpg").content
    file_name='test_input.jpg'
    with open(file_name, 'wb') as f:
        f.write(img_link)

    image_detection(model, image_path)
    


if __name__ == "__main__":
    main()
