<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.11.2" name="texture" tilewidth="64" tileheight="64" tilecount="4" columns="4">
 <image source="texture.png" width="256" height="64"/>
 <tile id="0">
  <properties>
   <property name="movible" type="bool" value="false"/>
   <property name="objetivo" type="bool" value="false"/>
   <property name="solido" type="bool" value="true"/>
   <property name="tipo" value="muro"/>
  </properties>
 </tile>
 <tile id="1">
  <properties>
   <property name="movible" type="bool" value="false"/>
   <property name="objetivo" type="bool" value="false"/>
   <property name="solido" type="bool" value="false"/>
   <property name="tipo" value="terreno"/>
  </properties>
 </tile>
 <tile id="2">
  <properties>
   <property name="movible" type="bool" value="true"/>
   <property name="objetivo" type="bool" value="false"/>
   <property name="solido" type="bool" value="true"/>
   <property name="tipo" value="caja"/>
  </properties>
 </tile>
 <tile id="3">
  <properties>
   <property name="movible" type="bool" value="false"/>
   <property name="objetivo" type="bool" value="true"/>
   <property name="solido" type="bool" value="false"/>
   <property name="tipo" value="punto"/>
  </properties>
 </tile>
</tileset>
