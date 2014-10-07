/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

(function () {
  var n = this, t = n._, r = {}, e = Array.prototype, u = Object.prototype, i = Function.prototype, a = e.push, o = e.slice, c = e.concat, l = u.toString, f = u.hasOwnProperty, s = e.forEach, p = e.map, h = e.reduce, v = e.reduceRight, d = e.filter, g = e.every, m = e.some, y = e.indexOf, b = e.lastIndexOf, x = Array.isArray, _ = Object.keys, j = i.bind, w = function (n) {
    return n instanceof w ? n : this instanceof w ? (this._wrapped = n, void 0) : new w(n)
  };
  "undefined" != typeof exports ? ("undefined" != typeof module && module.exports && (exports = module.exports = w), exports._ = w) : n._ = w, w.VERSION = "1.4.4";
  var A = w.each = w.forEach = function (n, t, e) {
    if (null != n)if (s && n.forEach === s)n.forEach(t, e); else if (n.length === +n.length) {
      for (var u = 0, i = n.length; i > u; u++)if (t.call(e, n[u], u, n) === r)return
    } else for (var a in n)if (w.has(n, a) && t.call(e, n[a], a, n) === r)return
  };
  w.map = w.collect = function (n, t, r) {
    var e = [];
    return null == n ? e : p && n.map === p ? n.map(t, r) : (A(n, function (n, u, i) {
      e[e.length] = t.call(r, n, u, i)
    }), e)
  };
  var O = "Reduce of empty array with no initial value";
  w.reduce = w.foldl = w.inject = function (n, t, r, e) {
    var u = arguments.length > 2;
    if (null == n && (n = []), h && n.reduce === h)return e && (t = w.bind(t, e)), u ? n.reduce(t, r) : n.reduce(t);
    if (A(n, function (n, i, a) {
      u ? r = t.call(e, r, n, i, a) : (r = n, u = !0)
    }), !u)throw new TypeError(O);
    return r
  }, w.reduceRight = w.foldr = function (n, t, r, e) {
    var u = arguments.length > 2;
    if (null == n && (n = []), v && n.reduceRight === v)return e && (t = w.bind(t, e)), u ? n.reduceRight(t, r) : n.reduceRight(t);
    var i = n.length;
    if (i !== +i) {
      var a = w.keys(n);
      i = a.length
    }
    if (A(n, function (o, c, l) {
      c = a ? a[--i] : --i, u ? r = t.call(e, r, n[c], c, l) : (r = n[c], u = !0)
    }), !u)throw new TypeError(O);
    return r
  }, w.find = w.detect = function (n, t, r) {
    var e;
    return E(n, function (n, u, i) {
      return t.call(r, n, u, i) ? (e = n, !0) : void 0
    }), e
  }, w.filter = w.select = function (n, t, r) {
    var e = [];
    return null == n ? e : d && n.filter === d ? n.filter(t, r) : (A(n, function (n, u, i) {
      t.call(r, n, u, i) && (e[e.length] = n)
    }), e)
  }, w.reject = function (n, t, r) {
    return w.filter(n, function (n, e, u) {
      return!t.call(r, n, e, u)
    }, r)
  }, w.every = w.all = function (n, t, e) {
    t || (t = w.identity);
    var u = !0;
    return null == n ? u : g && n.every === g ? n.every(t, e) : (A(n, function (n, i, a) {
      return(u = u && t.call(e, n, i, a)) ? void 0 : r
    }), !!u)
  };
  var E = w.some = w.any = function (n, t, e) {
    t || (t = w.identity);
    var u = !1;
    return null == n ? u : m && n.some === m ? n.some(t, e) : (A(n, function (n, i, a) {
      return u || (u = t.call(e, n, i, a)) ? r : void 0
    }), !!u)
  };
  w.contains = w.include = function (n, t) {
    return null == n ? !1 : y && n.indexOf === y ? n.indexOf(t) != -1 : E(n, function (n) {
      return n === t
    })
  }, w.invoke = function (n, t) {
    var r = o.call(arguments, 2), e = w.isFunction(t);
    return w.map(n, function (n) {
      return(e ? t : n[t]).apply(n, r)
    })
  }, w.pluck = function (n, t) {
    return w.map(n, function (n) {
      return n[t]
    })
  }, w.where = function (n, t, r) {
    return w.isEmpty(t) ? r ? null : [] : w[r ? "find" : "filter"](n, function (n) {
      for (var r in t)if (t[r] !== n[r])return!1;
      return!0
    })
  }, w.findWhere = function (n, t) {
    return w.where(n, t, !0)
  }, w.max = function (n, t, r) {
    if (!t && w.isArray(n) && n[0] === +n[0] && 65535 > n.length)return Math.max.apply(Math, n);
    if (!t && w.isEmpty(n))return-1 / 0;
    var e = {computed: -1 / 0, value: -1 / 0};
    return A(n, function (n, u, i) {
      var a = t ? t.call(r, n, u, i) : n;
      a >= e.computed && (e = {value: n, computed: a})
    }), e.value
  }, w.min = function (n, t, r) {
    if (!t && w.isArray(n) && n[0] === +n[0] && 65535 > n.length)return Math.min.apply(Math, n);
    if (!t && w.isEmpty(n))return 1 / 0;
    var e = {computed: 1 / 0, value: 1 / 0};
    return A(n, function (n, u, i) {
      var a = t ? t.call(r, n, u, i) : n;
      e.computed > a && (e = {value: n, computed: a})
    }), e.value
  }, w.shuffle = function (n) {
    var t, r = 0, e = [];
    return A(n, function (n) {
      t = w.random(r++), e[r - 1] = e[t], e[t] = n
    }), e
  };
  var k = function (n) {
    return w.isFunction(n) ? n : function (t) {
      return t[n]
    }
  };
  w.sortBy = function (n, t, r) {
    var e = k(t);
    return w.pluck(w.map(n,function (n, t, u) {
      return{value: n, index: t, criteria: e.call(r, n, t, u)}
    }).sort(function (n, t) {
      var r = n.criteria, e = t.criteria;
      if (r !== e) {
        if (r > e || r === void 0)return 1;
        if (e > r || e === void 0)return-1
      }
      return n.index < t.index ? -1 : 1
    }), "value")
  };
  var F = function (n, t, r, e) {
    var u = {}, i = k(t || w.identity);
    return A(n, function (t, a) {
      var o = i.call(r, t, a, n);
      e(u, o, t)
    }), u
  };
  w.groupBy = function (n, t, r) {
    return F(n, t, r, function (n, t, r) {
      (w.has(n, t) ? n[t] : n[t] = []).push(r)
    })
  }, w.countBy = function (n, t, r) {
    return F(n, t, r, function (n, t) {
      w.has(n, t) || (n[t] = 0), n[t]++
    })
  }, w.sortedIndex = function (n, t, r, e) {
    r = null == r ? w.identity : k(r);
    for (var u = r.call(e, t), i = 0, a = n.length; a > i;) {
      var o = i + a >>> 1;
      u > r.call(e, n[o]) ? i = o + 1 : a = o
    }
    return i
  }, w.toArray = function (n) {
    return n ? w.isArray(n) ? o.call(n) : n.length === +n.length ? w.map(n, w.identity) : w.values(n) : []
  }, w.size = function (n) {
    return null == n ? 0 : n.length === +n.length ? n.length : w.keys(n).length
  }, w.first = w.head = w.take = function (n, t, r) {
    return null == n ? void 0 : null == t || r ? n[0] : o.call(n, 0, t)
  }, w.initial = function (n, t, r) {
    return o.call(n, 0, n.length - (null == t || r ? 1 : t))
  }, w.last = function (n, t, r) {
    return null == n ? void 0 : null == t || r ? n[n.length - 1] : o.call(n, Math.max(n.length - t, 0))
  }, w.rest = w.tail = w.drop = function (n, t, r) {
    return o.call(n, null == t || r ? 1 : t)
  }, w.compact = function (n) {
    return w.filter(n, w.identity)
  };
  var R = function (n, t, r) {
    return A(n, function (n) {
      w.isArray(n) ? t ? a.apply(r, n) : R(n, t, r) : r.push(n)
    }), r
  };
  w.flatten = function (n, t) {
    return R(n, t, [])
  }, w.without = function (n) {
    return w.difference(n, o.call(arguments, 1))
  }, w.uniq = w.unique = function (n, t, r, e) {
    w.isFunction(t) && (e = r, r = t, t = !1);
    var u = r ? w.map(n, r, e) : n, i = [], a = [];
    return A(u, function (r, e) {
      (t ? e && a[a.length - 1] === r : w.contains(a, r)) || (a.push(r), i.push(n[e]))
    }), i
  }, w.union = function () {
    return w.uniq(c.apply(e, arguments))
  }, w.intersection = function (n) {
    var t = o.call(arguments, 1);
    return w.filter(w.uniq(n), function (n) {
      return w.every(t, function (t) {
        return w.indexOf(t, n) >= 0
      })
    })
  }, w.difference = function (n) {
    var t = c.apply(e, o.call(arguments, 1));
    return w.filter(n, function (n) {
      return!w.contains(t, n)
    })
  }, w.zip = function () {
    for (var n = o.call(arguments), t = w.max(w.pluck(n, "length")), r = Array(t), e = 0; t > e; e++)r[e] = w.pluck(n, "" + e);
    return r
  }, w.object = function (n, t) {
    if (null == n)return{};
    for (var r = {}, e = 0, u = n.length; u > e; e++)t ? r[n[e]] = t[e] : r[n[e][0]] = n[e][1];
    return r
  }, w.indexOf = function (n, t, r) {
    if (null == n)return-1;
    var e = 0, u = n.length;
    if (r) {
      if ("number" != typeof r)return e = w.sortedIndex(n, t), n[e] === t ? e : -1;
      e = 0 > r ? Math.max(0, u + r) : r
    }
    if (y && n.indexOf === y)return n.indexOf(t, r);
    for (; u > e; e++)if (n[e] === t)return e;
    return-1
  }, w.lastIndexOf = function (n, t, r) {
    if (null == n)return-1;
    var e = null != r;
    if (b && n.lastIndexOf === b)return e ? n.lastIndexOf(t, r) : n.lastIndexOf(t);
    for (var u = e ? r : n.length; u--;)if (n[u] === t)return u;
    return-1
  }, w.range = function (n, t, r) {
    1 >= arguments.length && (t = n || 0, n = 0), r = arguments[2] || 1;
    for (var e = Math.max(Math.ceil((t - n) / r), 0), u = 0, i = Array(e); e > u;)i[u++] = n, n += r;
    return i
  }, w.bind = function (n, t) {
    if (n.bind === j && j)return j.apply(n, o.call(arguments, 1));
    var r = o.call(arguments, 2);
    return function () {
      return n.apply(t, r.concat(o.call(arguments)))
    }
  }, w.partial = function (n) {
    var t = o.call(arguments, 1);
    return function () {
      return n.apply(this, t.concat(o.call(arguments)))
    }
  }, w.bindAll = function (n) {
    var t = o.call(arguments, 1);
    return 0 === t.length && (t = w.functions(n)), A(t, function (t) {
      n[t] = w.bind(n[t], n)
    }), n
  }, w.memoize = function (n, t) {
    var r = {};
    return t || (t = w.identity), function () {
      var e = t.apply(this, arguments);
      return w.has(r, e) ? r[e] : r[e] = n.apply(this, arguments)
    }
  }, w.delay = function (n, t) {
    var r = o.call(arguments, 2);
    return setTimeout(function () {
      return n.apply(null, r)
    }, t)
  }, w.defer = function (n) {
    return w.delay.apply(w, [n, 1].concat(o.call(arguments, 1)))
  }, w.throttle = function (n, t) {
    var r, e, u, i, a = 0, o = function () {
      a = new Date, u = null, i = n.apply(r, e)
    };
    return function () {
      var c = new Date, l = t - (c - a);
      return r = this, e = arguments, 0 >= l ? (clearTimeout(u), u = null, a = c, i = n.apply(r, e)) : u || (u = setTimeout(o, l)), i
    }
  }, w.debounce = function (n, t, r) {
    var e, u;
    return function () {
      var i = this, a = arguments, o = function () {
        e = null, r || (u = n.apply(i, a))
      }, c = r && !e;
      return clearTimeout(e), e = setTimeout(o, t), c && (u = n.apply(i, a)), u
    }
  }, w.once = function (n) {
    var t, r = !1;
    return function () {
      return r ? t : (r = !0, t = n.apply(this, arguments), n = null, t)
    }
  }, w.wrap = function (n, t) {
    return function () {
      var r = [n];
      return a.apply(r, arguments), t.apply(this, r)
    }
  }, w.compose = function () {
    var n = arguments;
    return function () {
      for (var t = arguments, r = n.length - 1; r >= 0; r--)t = [n[r].apply(this, t)];
      return t[0]
    }
  }, w.after = function (n, t) {
    return 0 >= n ? t() : function () {
      return 1 > --n ? t.apply(this, arguments) : void 0
    }
  }, w.keys = _ || function (n) {
    if (n !== Object(n))throw new TypeError("Invalid object");
    var t = [];
    for (var r in n)w.has(n, r) && (t[t.length] = r);
    return t
  }, w.values = function (n) {
    var t = [];
    for (var r in n)w.has(n, r) && t.push(n[r]);
    return t
  }, w.pairs = function (n) {
    var t = [];
    for (var r in n)w.has(n, r) && t.push([r, n[r]]);
    return t
  }, w.invert = function (n) {
    var t = {};
    for (var r in n)w.has(n, r) && (t[n[r]] = r);
    return t
  }, w.functions = w.methods = function (n) {
    var t = [];
    for (var r in n)w.isFunction(n[r]) && t.push(r);
    return t.sort()
  }, w.extend = function (n) {
    return A(o.call(arguments, 1), function (t) {
      if (t)for (var r in t)n[r] = t[r]
    }), n
  }, w.pick = function (n) {
    var t = {}, r = c.apply(e, o.call(arguments, 1));
    return A(r, function (r) {
      r in n && (t[r] = n[r])
    }), t
  }, w.omit = function (n) {
    var t = {}, r = c.apply(e, o.call(arguments, 1));
    for (var u in n)w.contains(r, u) || (t[u] = n[u]);
    return t
  }, w.defaults = function (n) {
    return A(o.call(arguments, 1), function (t) {
      if (t)for (var r in t)null == n[r] && (n[r] = t[r])
    }), n
  }, w.clone = function (n) {
    return w.isObject(n) ? w.isArray(n) ? n.slice() : w.extend({}, n) : n
  }, w.tap = function (n, t) {
    return t(n), n
  };
  var I = function (n, t, r, e) {
    if (n === t)return 0 !== n || 1 / n == 1 / t;
    if (null == n || null == t)return n === t;
    n instanceof w && (n = n._wrapped), t instanceof w && (t = t._wrapped);
    var u = l.call(n);
    if (u != l.call(t))return!1;
    switch (u) {
      case"[object String]":
        return n == t + "";
      case"[object Number]":
        return n != +n ? t != +t : 0 == n ? 1 / n == 1 / t : n == +t;
      case"[object Date]":
      case"[object Boolean]":
        return+n == +t;
      case"[object RegExp]":
        return n.source == t.source && n.global == t.global && n.multiline == t.multiline && n.ignoreCase == t.ignoreCase
    }
    if ("object" != typeof n || "object" != typeof t)return!1;
    for (var i = r.length; i--;)if (r[i] == n)return e[i] == t;
    r.push(n), e.push(t);
    var a = 0, o = !0;
    if ("[object Array]" == u) {
      if (a = n.length, o = a == t.length)for (; a-- && (o = I(n[a], t[a], r, e)););
    } else {
      var c = n.constructor, f = t.constructor;
      if (c !== f && !(w.isFunction(c) && c instanceof c && w.isFunction(f) && f instanceof f))return!1;
      for (var s in n)if (w.has(n, s) && (a++, !(o = w.has(t, s) && I(n[s], t[s], r, e))))break;
      if (o) {
        for (s in t)if (w.has(t, s) && !a--)break;
        o = !a
      }
    }
    return r.pop(), e.pop(), o
  };
  w.isEqual = function (n, t) {
    return I(n, t, [], [])
  }, w.isEmpty = function (n) {
    if (null == n)return!0;
    if (w.isArray(n) || w.isString(n))return 0 === n.length;
    for (var t in n)if (w.has(n, t))return!1;
    return!0
  }, w.isElement = function (n) {
    return!(!n || 1 !== n.nodeType)
  }, w.isArray = x || function (n) {
    return"[object Array]" == l.call(n)
  }, w.isObject = function (n) {
    return n === Object(n)
  }, A(["Arguments", "Function", "String", "Number", "Date", "RegExp"], function (n) {
    w["is" + n] = function (t) {
      return l.call(t) == "[object " + n + "]"
    }
  }), w.isArguments(arguments) || (w.isArguments = function (n) {
    return!(!n || !w.has(n, "callee"))
  }), "function" != typeof/./ && (w.isFunction = function (n) {
    return"function" == typeof n
  }), w.isFinite = function (n) {
    return isFinite(n) && !isNaN(parseFloat(n))
  }, w.isNaN = function (n) {
    return w.isNumber(n) && n != +n
  }, w.isBoolean = function (n) {
    return n === !0 || n === !1 || "[object Boolean]" == l.call(n)
  }, w.isNull = function (n) {
    return null === n
  }, w.isUndefined = function (n) {
    return n === void 0
  }, w.has = function (n, t) {
    return f.call(n, t)
  }, w.noConflict = function () {
    return n._ = t, this
  }, w.identity = function (n) {
    return n
  }, w.times = function (n, t, r) {
    for (var e = Array(n), u = 0; n > u; u++)e[u] = t.call(r, u);
    return e
  }, w.random = function (n, t) {
    return null == t && (t = n, n = 0), n + Math.floor(Math.random() * (t - n + 1))
  };
  var M = {escape: {"&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#x27;", "/": "&#x2F;"}};
  M.unescape = w.invert(M.escape);
  var S = {escape: RegExp("[" + w.keys(M.escape).join("") + "]", "g"), unescape: RegExp("(" + w.keys(M.unescape).join("|") + ")", "g")};
  w.each(["escape", "unescape"], function (n) {
    w[n] = function (t) {
      return null == t ? "" : ("" + t).replace(S[n], function (t) {
        return M[n][t]
      })
    }
  }), w.result = function (n, t) {
    if (null == n)return null;
    var r = n[t];
    return w.isFunction(r) ? r.call(n) : r
  }, w.mixin = function (n) {
    A(w.functions(n), function (t) {
      var r = w[t] = n[t];
      w.prototype[t] = function () {
        var n = [this._wrapped];
        return a.apply(n, arguments), D.call(this, r.apply(w, n))
      }
    })
  };
  var N = 0;
  w.uniqueId = function (n) {
    var t = ++N + "";
    return n ? n + t : t
  }, w.templateSettings = {evaluate: /<%([\s\S]+?)%>/g, interpolate: /<%=([\s\S]+?)%>/g, escape: /<%-([\s\S]+?)%>/g};
  var T = /(.)^/, q = {"'": "'", "\\": "\\", "\r": "r", "\n": "n", "	": "t", "\u2028": "u2028", "\u2029": "u2029"}, B = /\\|'|\r|\n|\t|\u2028|\u2029/g;
  w.template = function (n, t, r) {
    var e;
    r = w.defaults({}, r, w.templateSettings);
    var u = RegExp([(r.escape || T).source, (r.interpolate || T).source, (r.evaluate || T).source].join("|") + "|$", "g"), i = 0, a = "__p+='";
    n.replace(u, function (t, r, e, u, o) {
      return a += n.slice(i, o).replace(B, function (n) {
        return"\\" + q[n]
      }), r && (a += "'+\n((__t=(" + r + "))==null?'':_.escape(__t))+\n'"), e && (a += "'+\n((__t=(" + e + "))==null?'':__t)+\n'"), u && (a += "';\n" + u + "\n__p+='"), i = o + t.length, t
    }), a += "';\n", r.variable || (a = "with(obj||{}){\n" + a + "}\n"), a = "var __t,__p='',__j=Array.prototype.join," + "print=function(){__p+=__j.call(arguments,'');};\n" + a + "return __p;\n";
    try {
      e = Function(r.variable || "obj", "_", a)
    } catch (o) {
      throw o.source = a, o
    }
    if (t)return e(t, w);
    var c = function (n) {
      return e.call(this, n, w)
    };
    return c.source = "function(" + (r.variable || "obj") + "){\n" + a + "}", c
  }, w.chain = function (n) {
    return w(n).chain()
  };
  var D = function (n) {
    return this._chain ? w(n).chain() : n
  };
  w.mixin(w), A(["pop", "push", "reverse", "shift", "sort", "splice", "unshift"], function (n) {
    var t = e[n];
    w.prototype[n] = function () {
      var r = this._wrapped;
      return t.apply(r, arguments), "shift" != n && "splice" != n || 0 !== r.length || delete r[0], D.call(this, r)
    }
  }), A(["concat", "join", "slice"], function (n) {
    var t = e[n];
    w.prototype[n] = function () {
      return D.call(this, t.apply(this._wrapped, arguments))
    }
  }), w.extend(w.prototype, {chain: function () {
    return this._chain = !0, this
  }, value: function () {
    return this._wrapped
  }})
}).call(this);
