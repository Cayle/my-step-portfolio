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

package com.google.sps.data;

/** An item on a comments. */
public final class Comments {
  private final long id;
  private final String comment;
  private final String name;
  private final String email;
  private final long timestamp;

  public Comments(
    long id,
    String comment,
    String name,
    String email,
    long timestamp
  ) {
    this.id = id;
    this.comment = comment;
    this.name = name;
    this.email = email;
    this.timestamp = timestamp;
  }

  public String toString() {
    return "Comments: " + this.comment + " Name: " + this.name;
  }
}
