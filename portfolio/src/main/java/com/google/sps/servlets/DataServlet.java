// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.sps.data.Comments;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  static ArrayList<Long> commentIds = new ArrayList<Long>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    Query query = new Query("Comments")
    .addSort("timestamp", SortDirection.DESCENDING);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    int result_length = 0;

    for (Entity commentEntity : results.asIterable()) {
      long id = commentEntity.getKey().getId();
      commentIds.add(id);
      result_length++;
    }

    List<Comments> comments = new ArrayList<>();
    int comment_count_target = Integer.parseInt(
      request.getParameter("comment_count")
    );

    int comment_count = 0;

    for (Entity commentEntity : results.asIterable()) {
      long id = commentEntity.getKey().getId();
      String comment = (String) commentEntity.getProperty("comment");
      String name = (String) commentEntity.getProperty("name");
      long timestamp = (long) commentEntity.getProperty("timestamp");
      String email = (String) commentEntity.getProperty("email");
      Comments newComment = new Comments(id, comment, name, email, timestamp);
      comments.add(newComment);
      if (++comment_count >= comment_count_target) {
        break;
      }
    }

    response.setContentType("application/json;");
    Gson gson = new Gson();
    String commentResult = gson.toJson(comments);
    response.getWriter().println(commentResult);
  }

  //Receives the user comment and redirect to the homepage.
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException {
    UserService userService = UserServiceFactory.getUserService();

    String comment = request.getParameter("comment");
    String name = request.getParameter("name");
    long timestamp = System.currentTimeMillis();
    String email = userService.getCurrentUser().getEmail();

    Entity commentEntity = new Entity("Comments");
    commentEntity.setProperty("comment", comment);
    commentEntity.setProperty("timestamp", timestamp);
    commentEntity.setProperty("name", name);
    commentEntity.setProperty("email", email);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(commentEntity);
    response.sendRedirect("/index.html");
  }
}
