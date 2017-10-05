package scalacache.memoization

import scala.language.experimental.macros
import scala.language.higherKinds
import scala.reflect.macros.blackbox
import scalacache.Sync

class Macros(val c: blackbox.Context) {
  import c.universe._

  def memoize[F[_], G[_], S[X[_]] <: Sync[X], V](cache: c.Tree, ttl: c.Tree)(f: c.Tree)(mode: c.Tree): Tree = {
    commonMacroImpl(cache, { keyName =>
      q"""$cache.caching($keyName)($ttl)($f)($mode)"""
    })
  }

  def memoizeF[F[_], G[_], S[X[_]] <: Sync[X], V](cache: c.Tree, ttl: c.Tree)(f: c.Tree)(mode: c.Tree): Tree = {
    commonMacroImpl(cache, { keyName =>
      q"""$cache.cachingF($keyName)($ttl)($f)($mode)"""
    })
  }


  private def commonMacroImpl[F[_], G[_], S[X[_]] <: Sync[G], V: c.WeakTypeTag](cache: c.Tree, keyNameToCachingCall: (c.TermName) => c.Tree): Tree = {

    val enclosingMethodSymbol = findMethodSymbol()
    val classSymbol = findClassSymbol()

    /*
     * Gather all the info needed to build the cache key:
     * class name, method name and the method parameters lists
     */
    val classNameTree = getFullClassName(classSymbol)
    val classParamssTree = getConstructorParams(classSymbol)
    val methodNameTree = getMethodName(enclosingMethodSymbol)
    val methodParamssSymbols = c.internal.enclosingOwner.info.paramLists
    val methodParamssTree = paramListsToTree(methodParamssSymbols)

    val keyName = createKeyName()
    val scalacacheCall = keyNameToCachingCall(keyName)
    val tree = q"""
        val $keyName = $cache.config.memoization.toStringConverter.toString($classNameTree, $classParamssTree, $methodNameTree, $methodParamssTree)
        $scalacacheCall
        """
    //println(showCode(tree))
    //println(showRaw(tree, printIds = true, printTypes = true))
    tree
  }

  /**
   * Get the symbol of the method that encloses the macro,
   * or abort the compilation if we can't find one.
   */
  private def findMethodSymbol(): c.Symbol = {

    def findMethodSymbolRecursively(sym: Symbol): Symbol = {
      if (sym == null || sym == NoSymbol || sym.owner == sym)
        c.abort(
          c.enclosingPosition,
          "This memoize block does not appear to be inside a method. " +
            "Memoize blocks must be placed inside methods, so that a cache key can be generated."
        )
      else if (sym.isMethod)
        sym
      else
        findMethodSymbolRecursively(sym.owner)
    }

    findMethodSymbolRecursively(c.internal.enclosingOwner)
  }

  /**
   * Convert the given method symbol to a tree representing the method name.
   */
  private def getMethodName(methodSymbol: c.Symbol): c.Tree = {
    val methodName = methodSymbol.asMethod.name.toString
    // return a Tree
    q"$methodName"
  }

  private def findClassSymbol(): c.Symbol = {

    def findClassSymbolRecursively(sym: Symbol): Symbol = {
      if (sym == null)
        c.abort(c.enclosingPosition, "Encountered a null symbol while searching for enclosing class")
      else if (sym.isClass || sym.isModule)
        sym
      else
        findClassSymbolRecursively(sym.owner)
    }

    findClassSymbolRecursively(c.internal.enclosingOwner)
  }

  /**
   * Convert the given class symbol to a tree representing the fully qualified class name.
   *
   * @param classSymbol should be either a ClassSymbol or a ModuleSymbol
   */
  private def getFullClassName(classSymbol: c.Symbol): c.Tree = {
    val className = classSymbol.fullName
    // return a Tree
    q"$className"
  }

  private def getConstructorParams(classSymbol: c.Symbol): c.Tree = {
    if (classSymbol.isClass) {
      val symbolss = classSymbol.asClass.primaryConstructor.asMethod.paramLists
      if (symbolss == List(Nil)) {
        q"_root_.scala.collection.immutable.Vector.empty"
      } else {
        paramListsToTree(symbolss)
      }
    } else {
      q"_root_.scala.collection.immutable.Vector.empty"
    }
  }

  private def paramListsToTree(symbolss: List[List[c.Symbol]]): c.Tree = {
    val cacheKeyExcludeType = c.typeOf[cacheKeyExclude]
    def shouldExclude(s: c.Symbol) = {
      s.annotations.exists(a => a.tree.tpe == cacheKeyExcludeType)
    }
    val identss: List[List[Ident]] = symbolss.map(ss => ss.collect {
      case s if !shouldExclude(s) => Ident(s.name)
    })
    listToTree(identss.map(is => listToTree(is)))
  }

  /**
   * Convert a List[Tree] to a Tree representing `ArrayBuffer`
   */
  private def listToTree(ts: List[c.Tree]): c.Tree = {
    q"_root_.scala.collection.mutable.ArrayBuffer(..$ts)"
  }

  private def createKeyName(): TermName = {
    // We must create a fresh name for any vals that we define, to ensure we don't clash with any user-defined terms.
    // See https://github.com/cb372/scalacache/issues/13
    // (Note that c.freshName("key") does not work as expected.
    // It causes quasiquotes to generate crazy code, resulting in a MatchError.)
    c.freshName(c.universe.TermName("key"))
  }

}
