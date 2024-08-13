(ns meeruman.core
  (:use [seesaw.core]
        [seesaw.border]
        [seesaw.color]
        [seesaw.font])
  (:require [meeruman.http-req :as http]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [seesaw.util :as util])
  (:import
    (com.formdev.flatlaf.util SystemInfo)
    (java.awt Dimension Font Insets)
    (java.io File FileInputStream)
    (javax.swing Box DefaultCellEditor JFrame JTree UIManager)
    (javax.swing.event TableModelEvent TableModelListener)
    (javax.swing.plaf FontUIResource)
    (javax.swing.table DefaultTableModel)
    (javax.swing.tree DefaultMutableTreeNode)
    (org.apache.batik.anim.dom SVGDOMImplementation)
    (org.apache.batik.transcoder TranscoderInput TranscodingHints)
    (org.apache.batik.transcoder.image ImageTranscoder)
    (org.apache.batik.util SVGConstants)
    (org.fife.ui.rsyntaxtextarea RSyntaxTextArea SyntaxConstants Theme)
    (org.fife.ui.rtextarea RTextScrollPane)
    (org.graalvm.polyglot Context)
    (com.formdev.flatlaf.intellijthemes.materialthemeuilite FlatMaterialOceanicIJTheme)
    (meeruman SVGTranscoder)
    )
  (:gen-class)
  )

(def *documents (atom {}))
(defn tree-view []
  (let [root (DefaultMutableTreeNode. "Root")
        parent1 (DefaultMutableTreeNode. "Parent 1")]
    (.add root parent1)
    (doto (JTree. root)
      (.setPreferredSize (Dimension. 150 450))
      (.setMinimumSize (Dimension. 150 450))
      (.setMaximumSize (Dimension. 150 4500)))
    )
  )

(def *cwd (let [path-vec (str/split (.getPath (io/resource "beautify.js")) #"/")]
  (str (if (SystemInfo/isWindows) "" "/") (str/join "/" (subvec path-vec 1 (- (count path-vec) 2)))))
  )

(def *jsctx
  (let [options {"js.commonjs-require" "true", "js.commonjs-require-cwd" *cwd}]
    (.build (doto (Context/newBuilder (into-array String ["js"]))
              (.allowExperimentalOptions true)
              (.allowIO true)
              (.options options)
              ))))

(defn themed-syntax-text-area []
  (let [text-area (RSyntaxTextArea.)
        theme (Theme/load (io/input-stream (io/resource "org/fife/ui/rsyntaxtextarea/themes/monokai.xml")))]
    (.apply theme text-area)
    text-area
    )
  )

(defn init-documents [tab-name]
  (swap! *documents assoc tab-name {:uri-input            (text "http://127.0.0.1:4567/post-test")
                                    :param-uri-assoc      []
                                    :req-body             []
                                    :result-text-area     (themed-syntax-text-area)
                                    :pre-script-text-area (themed-syntax-text-area)
                                    :status               (label :text "")
                                    })
  )


(defn get-doc [tab doc] ((@*documents tab) doc))
(defn put-doc [tab doc new-doc] (swap! *documents assoc-in [tab doc] new-doc))

(defn read-svg [url w h]
  (let [dom-impl (SVGDOMImplementation/getDOMImplementation)
        hints (doto (TranscodingHints.)
                (.put ImageTranscoder/KEY_WIDTH (float w))
                (.put ImageTranscoder/KEY_HEIGHT (float h))
                (.put ImageTranscoder/KEY_DOM_IMPLEMENTATION dom-impl)
                (.put ImageTranscoder/KEY_DOCUMENT_ELEMENT_NAMESPACE_URI SVGConstants/SVG_NAMESPACE_URI)
                (.put ImageTranscoder/KEY_DOCUMENT_ELEMENT SVGConstants/SVG_SVG_TAG)
                )
        transcoder (doto (SVGTranscoder.)
                     (.setTranscodingHints hints)
                     )
        ]
    (.transcode transcoder (TranscoderInput. (FileInputStream. (File. url))) nil)
    (.getImage transcoder)
    )
  )


(defn generate-uri-params [tab-name]
  (prn (get-doc tab-name :param-uri-assoc))
  (map #(str (first %)
             (if (str/blank? (last %)) "" (str "=" (last %)))
             (if (every? str/blank? %) "" "&"))
       (get-doc tab-name :param-uri-assoc))
  )

(defn sync-uri-params [tab-name origin]
  (if (= origin :params-table)
    (let [uri-input (get-doc tab-name :uri-input)
          uri-text (text uri-input)
          query-str (apply str (generate-uri-params tab-name))
          trimmed-query-str (subs query-str 0 (dec (count query-str)))]
      (prn query-str)
      (if (nil? (str/index-of uri-text \?))
        (.setText uri-input (str uri-text "?" trimmed-query-str))
        (.setText uri-input (str (subs uri-text 0 (inc (str/index-of uri-text \?))) trimmed-query-str))
        )
      )
    )
  )

(defn register-table-event
  ([model tab-name doc default-row f]
   (register-table-event model tab-name doc default-row f (fn [] ()))
   )
  (
   [model tab-name doc default-row f finally]
   (.addTableModelListener model (reify TableModelListener (^void tableChanged [this ^TableModelEvent e]
                                                             (if (= (.getType e) (TableModelEvent/UPDATE))
                                                               (if (= (.getLastRow e) (dec (.getRowCount model)))
                                                                 (.addRow model (object-array default-row))
                                                                 )
                                                               )
                                                             (swap! *documents assoc-in [tab-name doc]
                                                                    (into [] (->> (range (dec (.getRowCount model)))
                                                                                  (map f
                                                                                       )))
                                                                    )
                                                             (finally)
                                                             )))
   ))

(defn process-uri-update []

  )

(defn rep-result-view []
  (let [text-area ((@*documents "Test1") :result-text-area)]
    (doto text-area
      (.setSyntaxEditingStyle SyntaxConstants/SYNTAX_STYLE_JSON)
      (.setCodeFoldingEnabled true)
      (.setEditable false))
    (RTextScrollPane. text-area)
    )
  )

(defn script-editor-view [area]
  (let [text-area ((@*documents "Test1") area)]
    (doto text-area
      (.setSyntaxEditingStyle SyntaxConstants/SYNTAX_STYLE_JAVASCRIPT)
      (.setCodeFoldingEnabled true))
    (RTextScrollPane. text-area)
    )
  )

(defn http-para-view [tab-name]
  (let [model (proxy [DefaultTableModel] [(object-array ["Key" "Value" "Description"]) 1]
                (isCellEditable [row col] true))
        table (table :model model)]
    (register-table-event model tab-name :param-uri-assoc '["" "" ""]
                          (fn [row]
                            [(.getValueAt model row 0)
                             (.getValueAt model row 1)]
                            )
                          #(sync-uri-params tab-name :params-table))
    (map #(doto (.getDefaultEditor table (.getColumnClass %)
                                   (.setClickCountToStart 1))) (range (.. table (getColumnModel) (getColumnCount))))
    (scrollable table)
    ))

(defn form-data-body-view [tab-name]
  (let [model (proxy [DefaultTableModel] [(object-array ["Key" "Type" "Value" "Description"]) 1]
                (isCellEditable [row col] true))
        table (table :model model)
        type-selector (combobox :model ["Text" "File"])
        type-column (.. table (getColumnModel) (getColumn 1))]
    (.setCellEditor type-column (DefaultCellEditor. type-selector))
    (.setMaxWidth type-column 50)
    (.setValueAt model "Text" 0 1)
    (register-table-event model tab-name :req-body '["" "Text" "" ""]
                          (fn [row]
                            [(str (.getValueAt model row 0) ":;" (.getValueAt model row 1))
                             (.getValueAt model row 2)])
                          (fn [] (prn (get-doc tab-name :req-body)) (put-doc tab-name :req-body (into {} (get-doc tab-name :req-body)))))
    (map #(doto (.getDefaultEditor table (.getColumnClass %)
                                   (.setClickCountToStart 1))) (range (.. table (getColumnModel) (getColumnCount))))
    (scrollable table)
    ))

(defn result-view []
  (tabbed-panel :tabs [{:title "Pretty" :content (rep-result-view)}
                       {:title "Raw" :content (label "Raw")}
                       {:title "Cookies" :content (label "Cookies")}
                       ])
  )

(defn body-view [tab-name]
  (let [body-type (combobox :model ["none", "form-data", "x-www-form-urlencoded"])
        body-card (card-panel :items [[(label "This request doesn't have a body.") "none"]
                                      [(form-data-body-view tab-name) "form-data"]
                                      [(label "x-www-form-urlencoded") "x-www-form-urlencoded"]
                                      ])
        ]
    (listen body-type :item-state-changed (fn [e] (show-card! body-card (str (selection body-type)))))
    (top-bottom-split body-type body-card :enabled? false :divider-size 0)
    )
  )

(defn http-test-inner-view [tab-name]
  (top-bottom-split (horizontal-panel :items [(tabbed-panel :tabs [{:title "Parameters" :content (http-para-view tab-name)}
                                                                   {:title "Body" :content (body-view tab-name)}
                                                                   {:title "Headers" :content (label "Headers")}
                                                                   {:title "Authorization" :content (label "Authorization")}
                                                                   {:title "Pre-request Script" :content (script-editor-view :pre-script-text-area)}]
                                                            )]) (horizontal-panel :items [(result-view)])
                    ))

(defn http-test-view [tab-name]
  (top-bottom-split (let [method-combobox (doto (combobox :model ["GET" "POST" "PUT" "PATCH" "DELETE" "HEAD" "OPTIONS"])
                                            (.setPreferredSize (Dimension. 100 30))
                                            (.setMaximumSize (Dimension. 100 30)))
                          send-button (button :text "Send")
                          save-button (button :text "Save")
                          uri-input (get-doc "Test1" :uri-input)]
                      (listen send-button :mouse-clicked (fn [e] (let [http-rep (http/dispatch-req (selection method-combobox)
                                                                                                   (text uri-input)
                                                                                                   {}
                                                                                                   (get-doc tab-name :req-body)
                                                                                                   )
                                                                       pre-script (get-doc "Test1" :pre-script-text-area)]
                                                                   (prn http-rep)
                                                                   (text! (get-doc "Test1" :result-text-area) (nth http-rep 0))
                                                                   (text! (get-doc "Test1" :status) (str/join "  " [
                                                                                                                    (str (nth http-rep 2) "ms")
                                                                                                                    (nth http-rep 1)
                                                                                                                    (str (get (http-rep 3) "content-length") "bytes")
                                                                                                                    ]))
                                                                   )))
                      (listen uri-input :insert-update (fn [e] (process-uri-update)))
                      (horizontal-panel :items [method-combobox
                                                (Box/createHorizontalStrut 10)
                                                uri-input
                                                (Box/createHorizontalStrut 10)
                                                send-button
                                                (Box/createHorizontalStrut 10)
                                                (button :text "Save")
                                                ]))
                    (horizontal-panel :items [(http-test-inner-view tab-name)])
                    :divider-location 43
                    :enabled? false
                    :divider-size 0
                    )
  )

(defn protocol-select-view [tab-name]
  (top-bottom-split (let [protocol-combobox (doto (combobox :model ["REST" "GRPC"])
                                              (.setPreferredSize (Dimension. 100 30))
                                              (.setMaximumSize (Dimension. 100 30)))]
                      (horizontal-panel :items [protocol-combobox
                                                (Box/createHorizontalStrut 10)
                                                (label :text "Untitled")
                                                (Box/createHorizontalGlue)
                                                (get-doc tab-name :status)
                                                (Box/createHorizontalStrut 20)
                                                ])
                      )
                    (http-test-view tab-name)
                    :divider-location 50
                    :enabled? false
                    :divider-size 0
                    )
  )

(defn tab-view []
  (let [default-font (nth (font-families) 0)
        font-resource (FontUIResource. default-font Font/PLAIN 20)
        new-tab-button (button :margin (util/to-insets [0 0 3 0]) :text "+" :halign :center :valign :center :font font-resource)
        p (tabbed-panel :tabs [{:title "Test1" :content (protocol-select-view "Test1")}
                               {:title new-tab-button}])]
    (.setEnabledAt p 1 false)
    p
    )
  )

(defn set-global-font []
  (let [default-font (font (nth (font-families) 0))
        font-resource (FontUIResource. default-font)
        ui-keys (into {} (UIManager/getDefaults))
        ]
    (doseq [[k v] ui-keys]
      (if (instance? FontUIResource v)
        (UIManager/put k font-resource)
        )
      )
    )
  )

(defn sidebar-card-view []
  (doto (card-panel :items [[(tree-view) "Collection"]
                            [(label "Environment") "Environment"]])
    (.setPreferredSize (Dimension. 150 450))
    (.setMinimumSize (Dimension. 150 450))
    (.setMaximumSize (Dimension. 150 4500))))

(defn sidebar-toolbar-view [sidebar-card]
  (prn (str *cwd "/resources/" "/icons/folder-light.svg"))
  (let [collection-button (button :icon (icon (read-svg (str *cwd "/resources/" "icons/folder-light.svg") 20.0 20.0)))
        env-button (button :icon (icon (read-svg (str *cwd "/resources/"  "icons/layers-light.svg") 20.0 20.0)))
        sidebar (toolbar :orientation :vertical :items [collection-button
                                                        env-button])]
    (listen collection-button :mouse-pressed (fn [e] (show-card! sidebar-card "Collection")))
    (listen env-button :mouse-pressed (fn [e] (show-card! sidebar-card "Environment")))
    (doto sidebar
      (.setPreferredSize (Dimension. 50 450))
      (.setMinimumSize (Dimension. 50 450))
      (.setMaximumSize (Dimension. 50 4500)))
    ))

(defn -main [& args]
  (try
    (UIManager/setLookAndFeel (FlatMaterialOceanicIJTheme.))
    (catch NullPointerException e (prn "Null"))
    )
  (set-global-font)
  (init-documents "Test1")
  ;(prn (.getPath (io/resource "beautify.js")))
  (try (http/dispatch-req "GET" "http://127.0.0.1" {} {})
       (catch Exception e))
  (JFrame/setDefaultLookAndFeelDecorated false)
  (invoke-later
    (let [
          sidebar-card (sidebar-card-view)
          sidebar-toolbar (sidebar-toolbar-view sidebar-card)
          f (frame :title "" :on-close :exit :height 600 :width 1000
                   :content (top-bottom-split (horizontal-panel :items [(label :border (empty-border :left 100) :text "MeeruMan")])
                                              (horizontal-panel :items [sidebar-toolbar
                                                                        sidebar-card
                                                                        (tab-view)])
                                              :divider-location 40
                                              :enabled? false
                                              :divider-size 0
                                              ))
          ]
      (if (SystemInfo/isMacFullWindowContentSupported)
        (do (.. f (getRootPane) (putClientProperty "apple.awt.fullWindowContent" true))
            (.. f (getRootPane) (putClientProperty "apple.awt.transparentTitleBar" true))))
      (-> f
          show!))))

