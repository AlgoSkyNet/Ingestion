/**
 * Copyright (C) 2014 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.ingestion.api.utils

import java.util.Properties

import com.stratio.ingestion.api.model.channel.AgentChannel
import com.stratio.ingestion.api.model.commons.{Agent, Attribute}
import com.stratio.ingestion.api.model.sink.AgentSink
import com.stratio.ingestion.api.model.source.AgentSource

/**
 * Created by eruiz on 19/10/15.
 */
object PropertiesToModel {
  //  val agentName = "a1"
  val sourceName = "sources"
  val channelName = "channels"
  val sinkName = "sinks"
  val typeName = "type"


  def propertiesToModel(nameFile: String) = {
    val p = new Properties()
    p.load(p.getClass().getResourceAsStream(nameFile))
    val agentName = p.keySet().toArray()(0).asInstanceOf[String].split('.')(0)
    //    p.list(System.out)
    val idSource = p.getProperty(agentName + "." + sourceName)
    val typeSource = p.getProperty(agentName + "." + sourceName + "." + idSource + "." + typeName)
    val settingsSourceBeforeFilter = getSettings(p, idSource, sourceName)
    val settingsSource = settingsSourceBeforeFilter.filter(att => att._type != channelName && att._type!=typeName)

    //Add union in channels and sinks
    //    val union = settingsSourceBeforeFilter.map(att => att._type=="channels")


    val idChannels = p.getProperty(agentName + "." + channelName).split(" ")
    val typeChannels = idChannels.map(id => (id,p.getProperty(agentName + "." + channelName + "." + id + "." +
      typeName)))
//    val settingsChannel = idChannels.map(idChan => getSettings(p, idChan, channelName))
    val settingsChannelBeforeFilter = typeChannels.map(idChan => (idChan._1,getSettings(p, idChan._1, channelName)))
    val settingsChannel = settingsChannelBeforeFilter.map(setting => setting._2.filter(att => att._type!=typeName))

    //    val settingsChannel = idChannels.map(idChan => getSettings(p, idChan, channelName)).flatMap(x =>x.seq)


    val idSinks = p.getProperty(agentName + "." + sinkName).split(" ")
    val typeSinks = idSinks.map(name => p.getProperty(agentName + "." + sinkName + "." + name + "." + typeName))
    val settingsSinksAndUnion = idSinks.map(idSink => getSettings(p, idSink, sinkName))
    val settingsSinks = settingsSinksAndUnion.map(x => x.filter(x => x._type!= "channel"))
//    val union = settingsSinksAndUnion.map(x => x.filter(x => x._type== channelName))


    import spray.json.DefaultJsonProtocol._

    val source = AgentSource(idSource, typeSource, "BuscaEnJSON", Seq(), settingsSource)

    // val chan1 = AgentChannel(idChannels(0), typeChannels(0), "", settingsChannel(1), source)


    //        val channels = (1 to typeChannels.length).foldLeft(Seq.empty[AgentChannel[String]]) {
    //          case (channels, i) =>
    //            channels :+ AgentChannel(idChannels(i), typeChannels(i),"" , settingsChannel(i), source)
    //        }

    val channels: Seq[AgentChannel[String]] =
      for {
        id <- idChannels
        typeChannel <- typeChannels
        setting <- settingsChannel
      } yield AgentChannel(id, typeChannel._2, "", setting, source)

    //val sink1 = AgentSink("idSinks", typoSink(0), nameSinks(0), settingsSink(0), chan1)
    //    val sink2 = AgentSink("idSinks", typoSink(1), nameSinks(1), settingsSink(1), chan2)
    val sinks: Seq[AgentSink[String]] =
      for {
        id <- idSinks
        typeSink <- typeSinks
        setting <- settingsSinks
        channel <- channels
      } yield AgentSink(id, typeSink, "", setting, channel)


    Agent(agentName, source, channels, sinks)

  }

  import spray.json.DefaultJsonProtocol._

  def getSettings(p: Properties, id: String, component: String): Seq[Attribute[String]] = {
    val agentName = p.keySet().toArray()(0).asInstanceOf[String].split('.')(0)

    val enuKeys = p.keys()
    var value: Seq[String] = Seq.empty[String]
    var attribute: Seq[Attribute[String]] = Seq.empty[Attribute[String]]
    while (enuKeys.hasMoreElements) {
      val key = enuKeys.nextElement().toString
      if (key.startsWith(agentName + "." + component + "." + id)) {
        value :+= p.getProperty(key)
        attribute :+= Attribute(id, key.split(agentName + "." + component + "." + id + ".")(1), "", true, p
          .getProperty
          (key))
      }
    }
    attribute
  }

}