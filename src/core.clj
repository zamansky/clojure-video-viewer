(ns  core
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults site-defaults]]
            [ring.middleware.reload :refer [wrap-reload]]
            [hiccup.core :as h]
            [hiccup.page :as hp]
            [hiccup.form :as hf]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.util.response :refer [response redirect content-type]]
            ))


(defn -main []
  (println "HELLO"))

(defn video-page [{:keys [session :as req]}]
  (let [
        videos (map #(.getName %) (file-seq (clojure.java.io/file "/home/zamansky/Sync/vv/resources/public/videos")))
        videos (drop 1 videos)

        ]
    (println videos)
    (hp/html5
    [:div
     (for [v videos]
       [:div
       [:h2.text-2xl v]
        [:video {:width 640 :height 480 :controls true}
         [:source {:src (str "/videos/" v) :type "video/mp4"}]
         ]]
        )
     ])
  ))



(defn login-page [{:keys [session] :as req}]
  (let [count (:count session 0)
      session (assoc session :count (inc count))
      page (hp/html5
   [:head (hp/include-css "/css/main.css") ]
   [:body
    [:div
     [:h1.bg-blue-200.text-4xl "Enter Password"]
     [:form {:action "/authenticate" :method "post"}
      [:input.bg-gray-400.p-2.m-2 {:type "password" :name "password"}]
      [:input.p-2.m-2.hover:bg-green-700.rounded {:type "submit" :value "Login"}]
     ]
    ]]
   )
        ]
  page
))

(defn index [{:keys [session :as req]}]
  (println session)
  (let [authenticated (:authenticated session nil)]
    (if authenticated
      (video-page req)
      (login-page req)
      )
    )
  
  )
(defn session-example [{:keys [session :as req]}]
  (let [count (:count session 0)
        session (assoc session :count (inc count))

        ]
    
  (println session)
  (println "index")
(-> 
  (content-type
  (response
    (hp/html5
   [:head (hp/include-css "/css/main.css") ]
   [:body
    [:div
     [:h1.bg-blue-400.text-4xl "hello World"]
     [:h2.text-4xl "Stuff"]
     [:h2.text-4xl ( :count session)]
    ]])
) "text/html")
(assoc :session session))
))

(defn authenticate [{:keys [:session :form-params] :as req}]
  (let [password (get form-params "password")
        isvalid (= password "abc")
        session (assoc session :authenticated isvalid)
        ]
    
    
  (->
   (redirect "/")
   (assoc :session session)
   ))
  )

(defroutes base-app
  (GET "/" [] index)
  (route/resources "/")
  (GET "/z" [] "<h1>ZZZZ</h1>")
  (POST "/authenticate" [] authenticate)
  (route/not-found "<h1>Page not found</h1>"))

(def my-defaults
  (assoc-in site-defaults [:security :anti-forgery] nil))
 (def app
   (-> base-app
       (wrap-defaults my-defaults)
 ))

(def server (jetty/run-jetty app {:port 8080 :join? false}))
;; (.stop server) and (.start server)

