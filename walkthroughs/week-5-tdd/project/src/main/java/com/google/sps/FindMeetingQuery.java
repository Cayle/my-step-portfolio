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
import java.util.Collections;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public final class FindMeetingQuery {

    Boolean NoMandatoryAttendees = false;
   /**
   * This method checks if the requested mandatory attendees
   * are included in the events given.
   * @param events It contains a collection of event objects
   * @param request It contains a MeetingRequest object.
   * @return Boolean -- states if the required attendees are included or not.
   */
  public Boolean areAttendeesIncluded(Collection<Event> events, MeetingRequest request){
      HashSet<String> bookedEventAttendees = new HashSet<>();
      for (Event event: events) {
          bookedEventAttendees.addAll(event.getAttendees());
      }

      bookedEventAttendees.retainAll(request.getAttendees());
      return !bookedEventAttendees.isEmpty();
  }



  /**
   * This method sorts the event objects in ascending order of the start time
   * of the events when attribute (TimeRange object)
   * @param events It contains a collection of event objects
   * @return List<Events> sorted version of the events parameter passed.
   */
  public List<Event> sortEvents(Collection<Event> events) {
      List<Event> newEvents = new ArrayList<>(events);
      
      List<TimeRange> sortedTimeRanges = new ArrayList<>();
      for (Event event : events) {
            sortedTimeRanges.add(event.getWhen());
      }

      Collections.sort(newEvents, Event.ORDER_BY_WHEN_START_TIME);
      Collections.sort(sortedTimeRanges, TimeRange.ORDER_BY_START);
        
      return newEvents;
  }


  /**
   * This method gets and saves the given booked TimeRanges while considering overlapping 
   * TimeRanges.
   * @param newEvents It contains a collection of sorted event objects.
   * @param request It contains a MeetingRequest object.
   * @return ArrayList<TimeRange> contains all the TimeRanges with previously overlapping ones merged.
   */
  public ArrayList<TimeRange> getAllBookedTimeRanges(List<Event> newEvents, MeetingRequest request) {
    ArrayList<TimeRange> attendeesBookedTimes = new ArrayList<TimeRange>();    
    if (newEvents.size() == 1)  {
        TimeRange currentEventTime = newEvents.get(0).getWhen();
        attendeesBookedTimes.add(currentEventTime);
        return attendeesBookedTimes;
    }


    Set<String> optionalAttendees = new HashSet<>(request.getOptionalAttendees());
  
    for (int k = 0; k < newEvents.size() - 1; k++){
        Set<String> eventAttendees = newEvents.get(k).getAttendees();
        if (optionalAttendees.containsAll(eventAttendees) & !NoMandatoryAttendees) {
            continue;
        }
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
    return attendeesBookedTimes;

  }


  /**
   * This method takes the refined booked TimeRanges and figures out those  
   * Timeranges that are available.
   * @param events It contains a collection of event objects
   * @param request It contains a MeetingRequest object.
   * @return Collection<TimeRange> contains all the possibly available TimeRanges.
   */
  public Collection<TimeRange> getAllTheAvailableTime(List<Event> newEvents, ArrayList<TimeRange> attendeesBookedTimes, MeetingRequest request) {
    ArrayList<TimeRange> availableTimeRanges = new ArrayList<>();
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

    //adds a final TimeRange if the previous meeting didn't last till the end of the day.
    Integer endOfTheDay = 24 * 60;
    if (eventStartTime < endOfTheDay) {
        TimeRange availableTimeToBook = TimeRange.fromStartEnd(eventStartTime, endOfTheDay, false);
        availableTimeRanges.add(availableTimeToBook);
    }

    Collection<TimeRange> availableTimeRangesWithOptional = new ArrayList<>();
    Set<String> optionalAttendees = new HashSet<>(request.getOptionalAttendees());
  
    //check the booked TimeRange of optional attendees to see if they can be fitted into the available TimeRange already derived.
    Integer optionalStartTime = 0;
    for (int j = 0; j < newEvents.size() ; j++)  {
        Set<String> eventAttendees = newEvents.get(j).getAttendees();
        if (optionalAttendees.containsAll(eventAttendees)) {
            TimeRange eventTime = newEvents.get(j).getWhen();
            for (int i = 0 ; i < availableTimeRanges.size(); i++) {
                if (availableTimeRanges.get(i).contains(eventTime)) {
                    if (optionalStartTime < eventTime.start()) {
                        TimeRange newAvailableTime = TimeRange.fromStartEnd(optionalStartTime, eventTime.start(), false);
                        availableTimeRangesWithOptional.add(newAvailableTime);
                    }
                    optionalStartTime = eventTime.end();
                }
            }
        }
    }
    if (optionalStartTime < endOfTheDay) {
        TimeRange availableTimeToBook = TimeRange.fromStartEnd(eventStartTime, endOfTheDay, false);
        availableTimeRangesWithOptional.add(availableTimeToBook);
    }

    //confirms if any additional TimeRange was added.
    if (optionalStartTime != 0) {
        return availableTimeRangesWithOptional;
    }

    return availableTimeRanges;
  }


  /**
   * This method uses the above helper functions to decide the available time 
   * based on the booked events and requested duration and attendees.
   * @param events It contains a collection of event objects
   * @param request It contains a MeetingRequest object.
   * @return Collection<TimeRange> contains all the time slot available.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    long whole_day = 24*60;

    if (request.getAttendees().isEmpty()) {
        if (request.getOptionalAttendees().isEmpty()) {
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }
    }

    if (request.getDuration() > whole_day) {
        return Arrays.asList();
    }
    if (!areAttendeesIncluded(events, request)) {
        if (request.getOptionalAttendees().isEmpty()){
            return Arrays.asList(TimeRange.WHOLE_DAY);
        }
        else {
            NoMandatoryAttendees = true;
        }  
    }
    List<Event> newEvents = sortEvents(events);    
  
    /*
    each event has one timeframe, our aim is to combine these timeframes together, in the best way possible:
        like takes care of overlapping events.
    */
   
    ArrayList<TimeRange> attendeesBookedTimes = getAllBookedTimeRanges(newEvents, request);

   
    //get the available time ranges, i.e those which aren't already booked,  and save it.
    Collection<TimeRange> availableTimeRanges = getAllTheAvailableTime(newEvents, attendeesBookedTimes, request);
    
   return availableTimeRanges;
  }
}
