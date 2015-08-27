(ns ^:figwheel-always sicp.core
    (:require))

(enable-console-print!)

;; DOM helpers
(defn by-id [id] 
  (.getElementById js/document id))

;; misc helpers
(defn for-each [p l]
  (doseq [i l]
    (p i)))

;;set size of canvas
(def CANVAS_WIDTH (.-innerWidth js/window))
(def CANVAS_HEIGHT (- (.-innerHeight js/window) 100))
(def CANVAS (by-id "canvas"))
(set! (.-width CANVAS) CANVAS_WIDTH)
(set! (.-height CANVAS) CANVAS_HEIGHT)

(def CTX (.getContext CANVAS "2d"))

;; vector procedures
(defn make-vect [x y]
  [x y])

(defn xcor-vect [v] 
  (nth v 0))

(defn ycor-vect [v] 
  (nth v 1))

(defn add-vect [v1 v2]
  (make-vect (+ (xcor-vect v1) (xcor-vect v2))
             (+ (ycor-vect v1) (ycor-vect v2))))

(defn sub-vect [v1 v2]
  (make-vect (- (xcor-vect v1) (xcor-vect v2))
             (- (ycor-vect v1) (ycor-vect v2))))

(defn scale-vect [s v]
  (make-vect (* (xcor-vect v) s) (* (ycor-vect v) s)))

;; Frame (ex 2.47)
(defn make-frame [origin edge1 edge2]
  [origin edge1 edge2])

(defn origin-frame [frame]
  (nth frame 0))

(defn edge1-frame [frame]
  (nth frame 1))

(defn edge2-frame [frame]
  (nth frame 2))

;;Frame Coord map
(defn frame-coord-map [frame]
  (fn [v] 
    (add-vect (origin-frame frame)
              (add-vect (scale-vect (xcor-vect v)
                                    (edge1-frame frame))
                        (scale-vect (ycor-vect v)
                                    (edge2-frame frame))))))
;;Segments
(defn make-segment [start-vect end-vect]
  [start-vect end-vect])

(defn start-segment [segment]
  (nth segment 0))

(defn end-segment [segment]
  (nth segment 1))


;;draw a line on the canvas
(defn draw-line! [start end]
    (println start end)
  (.beginPath CTX)
  (.moveTo CTX (xcor-vect start) (ycor-vect start))
  (.lineTo CTX (xcor-vect end) (ycor-vect end))
  (.stroke CTX))

;;Painter
(defn segments->painter [segment-list]
  (fn [frame]
    (for-each
      (fn [segment]
        (draw-line!
          ((frame-coord-map frame) (start-segment segment))
          ((frame-coord-map frame) (end-segment segment))))
        segment-list)
    ))

;; clear the canvas
(defn clear-canvas []
  (.clearRect CTX 0 0 CANVAS_WIDTH CANVAS_HEIGHT))

(def FRAME (make-frame [0 0] [0 500] [500 0]))
;;test
;;(draw-line! (make-vect 0 0) (make-vect 500 500))

;;Ex 2.49
(defn outline-frame [frame]
    ((segments->painter [(make-segment (make-vect 0 0) (make-vect 0 1))
                        (make-segment (make-vect 0 1) (make-vect 1 1))
                        (make-segment (make-vect 1 1) (make-vect 1 0))
                        (make-segment (make-vect 1 0) (make-vect 0 0))]
                       ) frame))

;;a) (outline-frame FRAME)




 

