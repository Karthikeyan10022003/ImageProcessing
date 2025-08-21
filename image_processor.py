import cv2
from ultralytics import YOLO

def image_detection(model, image_path):
    try:
    # Read the image
        frame = cv2.imread(image_path)

        if frame is None:
            print("❌ Error: Could not read image.")
            return

        # Run detection
        results = model(frame, conf=0.5, imgsz=1280)

        # Annotate the image
        annotated_frame = results[0].plot()

        # Show the result
        cv2.imshow("Snack Detection", annotated_frame)
        cv2.waitKey(0)
        

        # Save output image
        cv2.imwrite("output_image.jpg", annotated_frame)
    except(KeyboardInterrupt):
        print("❌ Error: Keyboard interrupt detected. Exiting.")
    finally:
        print("✅ Image detection completed successfully.")
        cv2.destroyAllWindows()

def video_detection(model, video_path):
    cap = cv2.VideoCapture(video_path)

    if not cap.isOpened():
        print("❌ Error: Could not open video.")
        return

    # Define output video writer
    fourcc = cv2.VideoWriter_fourcc(*"mp4v")  # codec
    out = cv2.VideoWriter(
        "output_video.mp4",
        fourcc,
        cap.get(cv2.CAP_PROP_FPS),
        (int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)), int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT)))
    )

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        # Run detection on the frame
        results = model(frame, conf=0.69, imgsz=1280)

        # Annotate the frame
        annotated_frame = results[0].plot()

        # Show in window
        cv2.imshow("Snack Detection - Video", annotated_frame)

        # Save to output video
        out.write(annotated_frame)

        # Exit if 'q' is pressed
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break

    cap.release()
    out.release()
    cv2.destroyAllWindows()


def main():
    # Load model only once
    model = YOLO(r"C:\Users\riota\Downloads\kitkat_and_lays.pt")

    image_path = r"D:\img_processing_trial\img_processing_trial\Screenshot 2025-08-11 144949.png"
    video_path = r"D:\img_processing_trial\img_processing_trial\istockphoto-2179808994-640_adpp_is.mp4"

    image_detection(model, image_path)
    # video_detection(model, video_path)


if __name__ == "__main__":
    main()
