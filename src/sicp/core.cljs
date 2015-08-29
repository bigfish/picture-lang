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
;;offset by .5 to keep lines sharp
(defn draw-line! [start end]
    (println start end)
  (.beginPath CTX)
  (.moveTo CTX (+ 0.5 (xcor-vect start)) (+ 0.5 (ycor-vect start)))
  (.lineTo CTX (+ 0.5(xcor-vect end)) (+ 0.5 (ycor-vect end)))
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

;;FRAME has origin at top left
(def FRAME (make-frame [0 0] [500 0] [0 500]))


;;test
;;(draw-line! (make-vect 0 0) (make-vect 500 500))

;;Ex 2.49
(defn outline-frame [frame]
    ((segments->painter [(make-segment (make-vect 0 0) (make-vect 0 1))
                        (make-segment (make-vect 0 1) (make-vect 1 1))
                        (make-segment (make-vect 1 1) (make-vect 1 0))
                        (make-segment (make-vect 1 0) (make-vect 0 0))]
                       ) frame))

(defn X-frame [frame]
  ((segments->painter [(make-segment [0 0] [1 1])
                       (make-segment [0 1] [1 0])])
   frame))

(defn diamond-frame [frame]
  ((segments->painter [(make-segment [0.5 0] [1 0.5])
                       (make-segment [1 0.5] [0.5 1])
                       (make-segment [0.5 1] [0 0.5])
                       (make-segment [0 0.5] [0.5 0])
                     ]) frame))

;;define WAVE segments, normalizing co-ords
(defn make-normalize-fn [width height]
  (fn [vect]
    (make-vect (/ (xcor-vect vect) width) (/ (ycor-vect vect) height))))

(defn wave [frame]
  (let [N (make-normalize-fn 14 20)]
    ((segments->painter [(make-segment (N [6 10]) (N [0 10]))
                         (make-segment (N [0 10]) (N [0 8]))
                         (make-segment (N [0 8]) (N [6 8]))
                         (make-segment (N [6 8]) (N [6 6]))
                         (make-segment (N [6 6]) (N [4 4]))
                         (make-segment (N [4 4]) (N [4 2]))
                         (make-segment (N [4 2]) (N [6 0]))
                         (make-segment (N [6 0]) (N [8 0]))
                         (make-segment (N [8 0]) (N [10 2]))
                         (make-segment (N [10 2]) (N [10 4]))
                         (make-segment (N [10 4]) (N [8 6]))
                         (make-segment (N [8 6]) (N [8 8]))
                         (make-segment (N [8 8]) (N [10 8]))
                         (make-segment (N [10 8]) (N [10 6]))
                         (make-segment (N [10 6]) (N [12 6]))
                         (make-segment (N [12 6]) (N [12 10]))
                         (make-segment (N [12 10]) (N [8 10]))
                         (make-segment (N [8 10]) (N [8 12]))
                         (make-segment (N [8 12]) (N [10 20]))
                         (make-segment (N [10 20]) (N [8 20]))
                         (make-segment (N [8 20]) (N [7 16]))
                         (make-segment (N [7 16]) (N [6 20]))
                         (make-segment (N [6 20]) (N [4 20]))
                         (make-segment (N [4 20]) (N [6 12]))
                         (make-segment (N [6 12]) (N [6 10]))
                         ]) frame)))

(defn transform-painter [painter origin corner1 corner2]
  (fn [frame]
      (let [m (frame-coord-map frame)
            new-origin (m origin)]
        (painter (make-frame new-origin
                             (sub-vect (m corner1) new-origin)
                             (sub-vect (m corner2) new-origin))))))
(defn flip-vert [painter]
  (transform-painter painter
                     (make-vect 0 1)
                     (make-vect 1 1)
                     (make-vect 0 0)))

(defn shrink-to-upper-right [painter]
  (transform-painter painter
                     (make-vect 0.5 0.5)
                     (make-vect 1.0 0.5)
                     (make-vect 0.5 1.0)))

(defn rotate90 [painter]
  (transform-painter painter
                     (make-vect 1 0)
                     (make-vect 1 1)
                     (make-vect 0 0)))

(defn squash-inwards [painter]
  (transform-painter painter
                     (make-vect 0 0)
                     (make-vect 0.65 0.35)
                     (make-vect 0.35 0.65)))
