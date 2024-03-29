/* KookyBot - a SDK of <https://www.kookapp.cn> for JVM platform
Copyright (C) 2022, zly2006 & contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.*/

package io.github.kookybot.contract

import io.github.kookybot.utils.Updatable
 class GuildRole(
     val id: Int,
     name: String,
     color: Int,
     position: Int,
     hoist: Int,
     mentionable: Int,
     permissions: Int,
 ) : Updatable {
     var name: String = name
         internal set
     var color: Int = color
         internal set
     var position: Int = position
         internal set
     var hoist: Int = hoist
         internal set
     var mentionable: Int = mentionable
         internal set
     var permissions: Int = permissions
         internal set

     /**
      * Don't use this method.
      */
     override fun update() {
         TODO()
     }
 }