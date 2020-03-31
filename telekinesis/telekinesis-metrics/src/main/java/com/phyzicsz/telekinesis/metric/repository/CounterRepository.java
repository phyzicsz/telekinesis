/*
 * Copyright 2020 phyzicsz <phyzics.z@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.phyzicsz.telekinesis.metric.repository;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.phyzicsz.telekinesis.metric.events.CounterCreateEvent;
import com.phyzicsz.telekinesis.metric.events.CounterIncrementEvent;
import com.phyzicsz.telekinesis.metric.events.CounterMonotonicIncrementEvent;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;

/**
 *
 * @author phyzicsz <phyzics.z@gmail.com>
 */
public class CounterRepository {
    private final Table<String, StringsKey, CounterMetricData> counterRepository;
    
    public CounterRepository(){
        counterRepository = HashBasedTable.create();
    }
    
    public void onCounterCreateEvent(CounterCreateEvent event){
        StringsKey key = new StringsKey(event.labels());
        
        CounterMetricData data = new CounterMetricData()
                .name(event.name())
                .help(event.help())
                .labelNames(event.labels())
                .counter(null);
        
        counterRepository.put(event.name(), key, data);
    }
    
    public void onCounterMonotonicIncrementEvent(CounterMonotonicIncrementEvent event){
        StringsKey key = new StringsKey(event.labelNames());
        
        CounterMetricData data = lookup(event.name(),event.labelNames());
        
        if(data.getCounter() == null){
            data.labelValues(event.labelValues());
            data.counter(new LongAdder());
            data.getCounter().increment();
        }else{
            data.getCounter().increment();
        }
        
        counterRepository.put(event.name(), key, data);
    }
    
    public void onCounterIncrementEvent(CounterIncrementEvent event){
        StringsKey key = new StringsKey(event.labelNames());
        
        CounterMetricData data = lookup(event.name(),event.labelNames());
        
        if(data.getCounter() == null){
            data.labelValues(event.labelValues());
            data.counter(new LongAdder());
            data.getCounter().add(event.inc());
        }else{
            data.getCounter().add(event.inc());
        }
        
        counterRepository.put(event.name(), key, data);
    }
    
    public CounterMetricData lookup(String name, String...labels){
        StringsKey key = new StringsKey(labels);
        CounterMetricData metricData = counterRepository.get(name, key);
        return metricData;
    }
    
    public void export(Writer stream){
        Set<String> keys = counterRepository.rowKeySet();
        
        for(String key: keys){
            Map<StringsKey,CounterMetricData> map = counterRepository.row(key);
            if(map.isEmpty()){
                continue;
            }
                
            Map.Entry<StringsKey,CounterMetricData> entry = map.entrySet().iterator().next();
        }
    }
    
    
}
