/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var app = {
    // Application Constructor
    initialize: function() {
        this.bindEvents();
        this.db.init();

        this.ui.lblName = $("#lblName");
        this.ui.iptName = $("#iptName");
        this.ui.listNames = $("#listNames");

        this.ui.changeName = $("#btnChangeName");
        this.ui.deleteNames = $("#btnDeleteNames");
        this.ui.menuDownload = $("#menuDownload");
        this.ui.menuShare = $("#menuShare");
        this.ui.menuDelete = $("#menuDelete");
        this.ui.contextMenu = $("#contextMenu");

        this.ui.changeName.click(function(e){
            e.preventDefault();
            if(app.ui.changeName){
                var name = app.ui.iptName.val();
                app.ui.fx.changeName(name);
                if(app.db.addName(name)){
                    app.loadNames();
                }
            }
        });

        this.ui.deleteNames.click(function(e){
            e.preventDefault();
            app.db.deleteNames();
            app.loadNames();
        });

        this.ui.menuDownload.click(function(e){
            e.preventDefault();
            app.downloadNames();
        });
        this.ui.menuDelete.click(function(e){
            e.preventDefault();
            app.db.deleteName($(this).closest("ul").data("index"));
            app.loadNames();
            app.ui.fx.closeContextMenu();
        });
        this.ui.menuShare.click(function(e){
            e.preventDefault();
            app.ui.fx.closeContextMenu();
        });

        this.loadNames();


            $(document).ready(function(){
                $(".inline").colorbox({inline:true, width:"50%"});
                $(".nolink").on('click', function(e){ e.preventDefault();});
            });        
    },
    downloadNames: function(){
        $.ajax({
            url: "http://thewheatfield.org/gdg/names.json",
            // url: "http://localhost/js/names.json",
            dataType: "json",
            method: "GET",
            success: function(data){
                var names = [];
                for(var i = 0; i < data.length; i++){
                    names.push(data[i]['name']);
                }
                app.db.saveNames(names);
                app.loadNames();
            }
        });
    },
    loadNames: function(){
        this.ui.listNames.html("");
        this.data.names = this.db.loadNames();
        for(var i = 0; this.data.names && i < this.data.names.length; i++){
            var item = $("<li></li>").data("index", i);
            var a = $("<a href='#' class='nolink'></a>");
            // item.text(this.data.names[i]);
            a.text(this.data.names[i]); item.append(a);
            item.on('touchstart', function(e){
                app.data.menuLongPress = $(this).data("index");
                app.data.menuLongPressTime = (new Date()).getTime();
                (function(time){
                    t=setTimeout(function(){
                        if(app.data.menuLongPress >= 0 && app.data.menuLongPressTime == time){
                            app.ui.fx.showContextMenu(app.data.menuLongPress);
                        }
                    },1000);
                })(app.data.menuLongPressTime);
            });
            item.on('touchend', function(e){
                app.ui.fx.resetContextMenuTimer();
            });
            item.on('touchmove', function(e){
                app.ui.fx.resetContextMenuTimer();
            });
            this.ui.listNames.append(item);
        }
        this.ui.listNames.find(".inline").colorbox({inline:true, width:"50%"});
    },
    // Bind Event Listeners
    //
    // Bind any events that are required on startup. Common events are:
    // 'load', 'deviceready', 'offline', and 'online'.
    bindEvents: function() {
        document.addEventListener('deviceready', this.onDeviceReady, false);
    },
    // deviceready Event Handler
    //
    // The scope of 'this' is the event. In order to call the 'receivedEvent'
    // function, we must explicity call 'app.receivedEvent(...);'
    onDeviceReady: function() {
        app.receivedEvent('deviceready');
    },
    // Update DOM on a Received Event
    receivedEvent: function(id) {
        var parentElement = document.getElementById(id);
        var listeningElement = parentElement.querySelector('.listening');
        var receivedElement = parentElement.querySelector('.received');

        listeningElement.setAttribute('style', 'display:none;');
        receivedElement.setAttribute('style', 'display:block;');

        console.log('Received Event: ' + id);
    },
    data : {},
    ui: {
        fx : {
            changeName: function(name){
                app.ui.lblName.text("Hello " + name);
            },
            resetContextMenuTimer: function(){
                app.data.menuLongPress = -1;
                app.data.menuLongPressTime = 0;
            },
            showContextMenu: function(index){
                app.ui.contextMenu.data("index", index);
                $(".inline").eq(0).trigger('click');
            },
            closeContextMenu: function(index){
                app.ui.contextMenu.colorbox.close();
            }
        }
    },
    db: {}
};
// app.data = (function(){
//     names = [];
// })();
app.db = (function(){
    function init(){
            if(!localStorage) return;

            for(var str in localStorage){
                    try{
                    id(str).value = localStorage[str];
                    }catch(err){}
            }
    }
    function add(id, value){
            if(!localStorage) return;
            localStorage[id] = value;
    }
    function del(id){
            localStorage.removeItem(id);
    }
    function deleteAll(){
            if(!localStorage) return;
            for(var str in localStorage){
                    localStorage.removeItem(str);
            }
    }
    function read(id){
        return localStorage[id];
    }
    var DB_KEY_NAMES = "names";
    function deleteNames(){
        del(DB_KEY_NAMES);
    }
    function deleteName(index){
        var names = loadNames(names);
        if(!names) return true;
        if(index && index < names.length){            
            var namesNew = [];
            for(var i = 0; i < names.length; i++){
                if(i == index) continue;
                namesNew.push(names[i]);
            }
            saveNames(namesNew);
            return true;
        }
        return false;
    }
    function addName(name){
        var names = this.loadNames();
        if(!names) names = [];
        names.unshift(name);
        saveNames(names);
        return true;
    }
    function saveNames(names){
        add(DB_KEY_NAMES, JSON.stringify(names));
    }
    function loadNames(){
        var namesAsString = localStorage[DB_KEY_NAMES];
        if(namesAsString)
            return JSON.parse(namesAsString);
        return null;
    }

    return {
        init: init,
        addName: addName,
        deleteName: deleteName,
        deleteNames: deleteNames,
        saveNames: saveNames,
        loadNames: loadNames
    }
})();
