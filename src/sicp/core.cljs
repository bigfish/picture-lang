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

(defn log [msg]
  (.log js/console msg))

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
  (.beginPath CTX)
  (.moveTo CTX (+ 0.5 (xcor-vect start)) (+ 0.5 (ycor-vect start)))
  (.lineTo CTX (+ 0.5 (xcor-vect end)) (+ 0.5 (ycor-vect end)))
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

(defn ident [painter]
  painter)

;; clear the canvas
(defn clear-canvas! []
  (.clearRect CTX 0 0 CANVAS_WIDTH CANVAS_HEIGHT))

;;FRAME has origin at top left
(def FRAME(make-frame [0 0] [500 0] [0 500]))


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

(defn flip-horiz [painter]
  (transform-painter painter
                     (make-vect 1 0)
                     (make-vect 0 0)
                     (make-vect 1 1)))

(defn shrink-to-upper-right [painter]
  (transform-painter painter
                     (make-vect 0.5 0.5)
                     (make-vect 1.0 0.5)
                     (make-vect 0.5 1.0)))

(defn rotate90 [painter]
  (transform-painter painter
                     (make-vect 0 1)
                     (make-vect 0 0)
                     (make-vect 1 1)))

(defn rotate180 [painter]
  (transform-painter painter
                     (make-vect 1 1)
                     (make-vect 0 1)
                     (make-vect 1 0)))

(defn rotate270 [painter]
  (transform-painter painter
                     (make-vect 1 0)
                     (make-vect 1 1)
                     (make-vect 0 0)))


(defn squash-inwards [painter]
  (transform-painter painter
                     (make-vect 0 0)
                     (make-vect 0.65 0.35)
                     (make-vect 0.35 0.65)))

(defn beside [painter1 painter2]
  (let [split-point (make-vect 0.5 0)
        paint-left (transform-painter painter1
                                      (make-vect 0 0)
                                      split-point
                                      (make-vect 0 1))
        paint-right (transform-painter painter2
                                       split-point
                                       (make-vect 1 0)
                                       (make-vect 0.5 1))]
    (fn [frame]
      (paint-left frame)
      (paint-right frame))))


(defn below [painter1 painter2]
  (let [split-point (make-vect 0 0.5)
        paint-bottom (transform-painter painter1
                                     (make-vect 0 0)
                                     (make-vect 1 0)
                                     split-point)
        paint-top (transform-painter painter2
                                     split-point
                                     (make-vect 1 0.5)
                                     (make-vect 0 1))]
    (fn [frame]
      (paint-bottom frame)
      (paint-top frame))))


(defn wave2 [frame]
  ((beside wave (flip-vert wave))
     frame))

;(defn wave4 [frame]
  ;((below wave2 wave2)
   ;frame))

(defn flipped-pairs [painter]
  (let [painter2 (beside painter (flip-vert painter))]
    (below painter2 painter2)))

(defn wave4 [frame] 
  ((flipped-pairs wave) frame))

(defn right-split [painter n]
  (if (= n 0)
    painter
    (let [smaller (right-split painter (- n 1))]
      (beside painter (below smaller smaller)))))

(defn up-split [painter n]
  (if (= n 0)
    painter
    (let [smaller (up-split painter (- n 1))]
      (below (beside smaller smaller) painter ))))

(defn corner-split [painter n]
  (if (= n 0)
    painter
    (let [up (up-split painter (- n 1))
          right (right-split painter (- n 1))
          top-left (beside up up)
          bottom-right (below right right)
          corner (corner-split painter (- n 1))]
      (beside (below top-left painter )
              (below corner bottom-right )))))

(defn square-limit [painter n]
 (let [quarter (corner-split painter n)
       half (beside (flip-horiz quarter) quarter)]
   (below half (flip-vert half))))

(defn square-of-four [tl tr bl br]
  (fn [painter]
    (let [top (beside (tl painter) (tr painter))
          bottom (beside (bl painter) (br painter))]
      (below top bottom))))

(defn flipped-pairs2 [painter]
  (let [combine4 (square-of-four ident flip-vert
                                 ident flip-vert)]
    (combine4 painter)))

(defn square-limit2 [painter n]
  (let [combine4 (square-of-four flip-horiz ident
                                 rotate180 flip-vert)]
    (combine4 (corner-split painter n))))

;;paint 2 painters, without transforming the frame they paint
(defn double-painter [p1 p2]
  (fn [frame]
      (p1 frame)
      (p2 frame)))

(defn empty-painter [frame])

;;make a frame from a tile
(defn make-tile [xcor ycor width height]
  (make-frame (make-vect xcor ycor)
              (make-vect (+ width xcor) ycor)
              (make-vect xcor (+ height ycor))))

(defn tile-width [tile]
  (- (xcor-vect (edge1-frame tile))
     (xcor-vect (origin-frame tile))))

(defn tile-height [tile]
  (- (ycor-vect (edge2-frame tile))
     (ycor-vect (origin-frame tile))))

(defn tile-painter [painter tile]
 (transform-painter painter
                    (origin-frame tile)
                    (edge1-frame tile)
                    (edge2-frame tile)))

(defn shift-tile [tile shift]
  (let [shift-vect (make-vect (* (tile-width tile) (xcor-vect shift))
                              (* (tile-height tile) (ycor-vect shift)))]
    (make-frame (add-vect (origin-frame tile) shift-vect)
               (add-vect (edge1-frame tile) shift-vect)
               (add-vect (edge2-frame tile) shift-vect))))

;;painter transformers
(defn ident-painter [painter tile n]
  (tile-painter painter tile))

(defn odd-tile-painter [painter tile n]
  (if (odd? n)
    (tile-painter painter tile)
    empty-painter))

;;use tile size to shift over
;;dir is a vector indicating direction to shift
(defn tile-sequence-painter [painter n tile dir transform-painter]
    (let [paint-tile (transform-painter painter tile n)
          next-tile (shift-tile tile dir)]
      (if (= 0 n) 
        paint-tile
        (double-painter paint-tile
                        (tile-sequence-painter painter (- n 1) next-tile dir transform-painter))
        )))

(defn tile-horiz [painter n]
  (let [dir (make-vect 1 0)
        tile (make-tile 0 0 (/ 1 n) 1)]
    (tile-sequence-painter painter n tile dir ident-painter)))

(defn tile-vert [painter n]
  (let [dir (make-vect 0 1)
        tile (make-tile 0 0 1 (/ 1 n))]
    (tile-sequence-painter painter n tile dir ident-painter)))

(defn tile-horiz-odd [painter n]
  (let [dir (make-vect 1 0)
        tile (make-tile 0 0 (/ 1 n) 1)]
    (tile-sequence-painter painter n tile dir odd-tile-painter)))

(defn tile-vert-odd [painter n]
  (let [dir (make-vect 0 1)
        tile (make-tile 0 0 1 (/ 1 n))]
    (tile-sequence-painter painter n tile dir odd-tile-painter)))

(defn tile-plane [painter rows cols]
    (tile-vert (tile-horiz painter cols) rows))
