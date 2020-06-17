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

package com.google.sps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.HashSet;

public final class FindMeetingQuery {
  public Boolean areAttendeesIncluded(Collection<Event> events, MeetingRequest request){
      HashSet<String> bookedEventAttendees = new HashSet<>();
      for (Event event: events) {
          bookedEventAttendees.addAll(event.getAttendees());
      }

      bookedEventAttendees.retainAll(request.getAttendees());
      return !bookedEventAttendees.isEmpty();
  }
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long whole_day = 24*60;

    if (request.getAttendees().isEmpty()) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    else if (request.getDuration() > whole_day) {
        return Arrays.asList();
    }
    else if (!areAttendeesIncluded(events, request)) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    ArrayList<Event> newEvents = new ArrayList<>(events);

    //each event has one timeframe, our aim is to combine these timeframes together, in the best way possible:
        //like takes care of overlapping events.
    ArrayList<TimeRange> attendeesBookedTimes = new ArrayList<TimeRange>();    
    if (newEvents.size() == 1)  {
        TimeRange currentEventTime = newEvents.get(0).getWhen();
        attendeesBookedTimes.add(currentEventTime);
    }
    for (int k = 0; k < newEvents.size() - 1; k++){
        TimeRange currentEventTime = newEvents.get(k).getWhen();
        TimeRange nextEventTime = newEvents.get(k+1).getWhen();
        if (currentEventTime.overlaps(nextEventTime)) {
            Integer currentStartTime = currentEventTime.start();
            Integer nextStartTime = nextEventTime.start();
            Integer combinedStartTime = currentStartTime < nextStartTime ? currentStartTime : nextStartTime;
            
            Integer currentEndTime =  currentEventTime.end();
            Integer nextEndTime = nextEventTime.end();
            Integer combinedEndTime = currentEndTime > nextEndTime ? currentEndTime : nextEndTime;
            
            TimeRange combinedTimeRange = TimeRange.fromStartEnd(combinedStartTime, combinedEndTime, false);
            attendeesBookedTimes.add(combinedTimeRange);
        }
        else {
            attendeesBookedTimes.add(currentEventTime);
            attendeesBookedTimes.add(nextEventTime);
        }
    }

    //get the available time ranges, i.e those which aren't already booked,  and save it.
    Collection<TimeRange> availableTimeRanges = new ArrayList<>();
    Integer eventStartTime = 0;
    for (int l = 0; l < attendeesBookedTimes.size(); l++) {
        TimeRange bookedTimeRange = attendeesBookedTimes.get(l);
        if (eventStartTime <  bookedTimeRange.start()) {
            Integer availableTimeToMeet = bookedTimeRange.start() - eventStartTime;
            if (availableTimeToMeet >= request.getDuration()) {
                TimeRange availableTimeToBook = TimeRange.fromStartEnd(eventStartTime, bookedTimeRange.start(), false);
                availableTimeRanges.add(availableTimeToBook);
            }
            eventStartTime = bookedTimeRange.end();
        }
        else if ( eventStartTime == bookedTimeRange.start()){
            eventStartTime = bookedTimeRange.end();
        }
    }

    Integer endOfTheDay = 24 * 60;
    if (eventStartTime < endOfTheDay) {
        TimeRange availableTimeToBook = TimeRange.fromStartEnd(eventStartTime, endOfTheDay, false);
        availableTimeRanges.add(availableTimeToBook);
    }
   return availableTimeRanges;
  }
}
