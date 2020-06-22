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

public final class FindMeetingQueryBogus {
  public Boolean areAttendeesIncluded(Collection<Event> events, MeetingRequest request){
      HashSet<String> bookedEventAttendees = new HashSet<>();
      for (Event event: events) {
          bookedEventAttendees.addAll(event.getAttendees());
      }

      for(String xx : bookedEventAttendees) {
          System.out.println(xx);
      }

      bookedEventAttendees.retainAll(request.getAttendees());
      System.out.println(bookedEventAttendees.isEmpty());
      return !bookedEventAttendees.isEmpty();
  }
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // throw new UnsupportedOperationException("TODO: Implement this method.");
    long whole_day = 24*60;

    if (request.getAttendees().isEmpty()) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    if (request.getDuration() > whole_day) {
        return Arrays.asList();
    }
    if (!areAttendeesIncluded(events, request)) {
        return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    System.out.println(!areAttendeesIncluded(events, request));

    ArrayList<Integer[]> isAttendeeBooked = new ArrayList<Integer[]>(); 

    Collection<TimeRange> time_result = new ArrayList<TimeRange>();
    for(Event event: events) {
        Integer start = event.getWhen().start();
        Integer end = event.getWhen().end();
        Integer duration = event.getWhen().duration();
        System.out.println(start + " " + end + " -- " + duration);
        Integer[] attendeeBusyTimeRange = new Integer[3];
        if (isAttendeeBooked.isEmpty()) {
            attendeeBusyTimeRange[0] = start;
            attendeeBusyTimeRange[1] = duration;
            attendeeBusyTimeRange[2] = end;
            isAttendeeBooked.add(attendeeBusyTimeRange);
            continue;
        }
        int length = isAttendeeBooked.size();
        for (int i = 0 ; i < length; i++) {
            Integer[] attendeeTime = isAttendeeBooked.get(i);
            if (Integer.compare(start, attendeeTime[2]) > 0) {
                attendeeBusyTimeRange[0] = start;
                attendeeBusyTimeRange[1] = duration;
                attendeeBusyTimeRange[2] = end;
                isAttendeeBooked.add(attendeeBusyTimeRange);   
            }
            else if(Integer.compare(start, attendeeTime[2]) <= 0){
                if(Integer.compare(end, attendeeTime[2]) < 0){
                    continue;
                }
                    attendeeBusyTimeRange[0] = attendeeTime[2];
                    attendeeBusyTimeRange[1] = end - attendeeTime[2];
                    attendeeBusyTimeRange[2] = end;
                    isAttendeeBooked.add(attendeeBusyTimeRange);   
            }
        }
        
    }

    for (Integer[] xx: isAttendeeBooked) {
        System.out.println(Arrays.toString(xx));
    }

    Integer starttimeInMinutes = 0;
    Integer endTimeInMinutes = 1440;
    Integer totalMinutesInADay = 1440;

    for (int j = 0; j < isAttendeeBooked.size(); j++) {
        Integer[] attendeeTimes = isAttendeeBooked.get(j);
        if (Integer.compare(starttimeInMinutes, attendeeTimes[0]) < 0) {
            Integer duration = Integer.valueOf((int) request.getDuration());
            Integer availableTimeRange = attendeeTimes[0] - starttimeInMinutes;
            System.out.println(availableTimeRange);
            if (Integer.compare(availableTimeRange, duration) >= 0){
                 System.out.println("Yes" + duration);
                TimeRange availableTime = TimeRange.fromStartEnd(starttimeInMinutes, attendeeTimes[0], false);
                time_result.add(availableTime);
            }
            starttimeInMinutes = attendeeTimes[2];
        }
        else if (Integer.compare(starttimeInMinutes, attendeeTimes[0]) == 0 ) {
            starttimeInMinutes = attendeeTimes[2];
        }
    }
    
    if (Integer.compare(starttimeInMinutes, totalMinutesInADay) < 0) {
        TimeRange availableTime = TimeRange.fromStartEnd(starttimeInMinutes, totalMinutesInADay, false);
        time_result.add(availableTime);
    }

    System.out.println(Arrays.toString(time_result.toArray()));

    System.out.println(Arrays.asList(time_result));
    System.out.println(Arrays.asList(time_result.toArray()));

    return time_result;
  }
}
